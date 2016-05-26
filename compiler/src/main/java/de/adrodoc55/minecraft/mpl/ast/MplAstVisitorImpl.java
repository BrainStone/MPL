package de.adrodoc55.minecraft.mpl.ast;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static de.adrodoc55.minecraft.mpl.ast.chainparts.MplIntercept.INTERCEPTED;
import static de.adrodoc55.minecraft.mpl.ast.chainparts.MplNotify.NOTIFY;
import static de.adrodoc55.minecraft.mpl.commands.Conditional.CONDITIONAL;
import static de.adrodoc55.minecraft.mpl.commands.Conditional.UNCONDITIONAL;
import static de.adrodoc55.minecraft.mpl.commands.Mode.CHAIN;
import static de.adrodoc55.minecraft.mpl.commands.Mode.IMPULSE;
import static de.adrodoc55.minecraft.mpl.commands.Mode.REPEAT;
import static de.adrodoc55.minecraft.mpl.commands.chainlinks.ReferencingCommand.REF;
import static de.adrodoc55.minecraft.mpl.compilation.CompilerOptions.CompilerOption.DEBUG;
import static de.adrodoc55.minecraft.mpl.compilation.CompilerOptions.CompilerOption.TRANSMITTER;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.annotations.VisibleForTesting;

import de.adrodoc55.minecraft.coordinate.Coordinate3D;
import de.adrodoc55.minecraft.coordinate.Orientation3D;
import de.adrodoc55.minecraft.mpl.ast.chainparts.ChainPart;
import de.adrodoc55.minecraft.mpl.ast.chainparts.Dependable;
import de.adrodoc55.minecraft.mpl.ast.chainparts.ModifiableChainPart;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplBreakpoint;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplCommand;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplIf;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplIntercept;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplNotify;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplStart;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplStop;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplWaitfor;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplWhile;
import de.adrodoc55.minecraft.mpl.ast.chainparts.program.MplProcess;
import de.adrodoc55.minecraft.mpl.ast.chainparts.program.MplProgram;
import de.adrodoc55.minecraft.mpl.chain.ChainContainer;
import de.adrodoc55.minecraft.mpl.chain.CommandChain;
import de.adrodoc55.minecraft.mpl.commands.Conditional;
import de.adrodoc55.minecraft.mpl.commands.Mode;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.ChainLink;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.Command;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.InternalCommand;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.InvertingCommand;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.MplSkip;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.NormalizingCommand;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.ReferencingCommand;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.ReferencingTestforSuccessCommand;
import de.adrodoc55.minecraft.mpl.compilation.CompilerOptions;
import de.adrodoc55.minecraft.mpl.interpretation.IllegalModifierException;
import de.adrodoc55.minecraft.mpl.interpretation.ModifierBuffer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author Adrodoc55
 */
public class MplAstVisitorImpl implements MplAstVisitor {
  private ChainContainer container;
  @VisibleForTesting
  List<CommandChain> chains = new ArrayList<>();
  @VisibleForTesting
  List<ChainLink> commands = new ArrayList<>();
  private final CompilerOptions options;

  private boolean addBreakpointProcess;

  public MplAstVisitorImpl(CompilerOptions options) {
    this.options = checkNotNull(options, "options == null!");
  }

  @Override
  public ChainContainer getResult() {
    return container;
  }

  /**
   * Returns the relative count to the given {@link ChainLink}. If {@code ref} is null the returned
   * count will reference the first link in {@link #commands}.
   *
   * @param ref the {@link ChainLink} to search for
   * @return the count to ref
   * @throws IllegalArgumentException if {@code ref} is not found
   * @throws NullPointerException if {@code ref} is null
   */
  private int getCountToRef(ChainLink ref) throws IllegalArgumentException, NullPointerException {
    checkNotNull(ref, "ref == null!");
    for (int i = commands.size() - 1; i >= 0; i--) {
      if (ref == commands.get(i)) {
        return -commands.size() + i;
      }
    }
    throw new IllegalArgumentException("The given ref was not found in commands.");
  }

  public String getStartCommand(String ref) {
    if (options.hasOption(TRANSMITTER)) {
      return "setblock " + ref + " redstone_block";
    } else {
      return "blockdata " + ref + " {auto:1b}";
    }
  }

  public String getStopCommand(String ref) {
    if (options.hasOption(TRANSMITTER)) {
      return "setblock " + ref + " stone";
    } else {
      return "blockdata " + ref + " {auto:0b}";
    }
  }

  private ReferencingCommand newReferencingStartCommand(boolean conditional, int relative) {
    return new ReferencingCommand(getStartCommand(REF), conditional, relative);
  }

  private ReferencingCommand newReferencingStopCommand(boolean conditional, int relative) {
    return new ReferencingCommand(getStopCommand(REF), conditional, relative);
  }

  private void addRestartBackref(ChainLink chainLink, boolean conditional) {
    commands.add(newReferencingStopCommand(conditional, getCountToRef(chainLink)));
    commands.add(newReferencingStartCommand(true, getCountToRef(chainLink)));
  }

  private void addTransmitterReceiverCombo(boolean internal) {
    if (options.hasOption(TRANSMITTER)) {
      commands.add(new MplSkip(internal));
      commands.add(new InternalCommand(getStopCommand("${this - 1}"), Mode.IMPULSE));
    } else {
      commands.add(new InternalCommand(getStopCommand("~ ~ ~"), Mode.IMPULSE));
    }
  }

  @Override
  public void visitProgram(MplProgram program) {
    chains = new ArrayList<>(1);
    Orientation3D orientation = program.getOrientation();
    Coordinate3D max = program.getMax();
    CommandChain install = visitUnInstall(program.getInstall());
    CommandChain uninstall = visitUnInstall(program.getUninstall());

    chains = new ArrayList<>(program.getProcesses().size());
    for (MplProcess process : program.getProcesses()) {
      process.accept(this);
    }
    if (addBreakpointProcess) {
      addBreakpointProcess(program);
    }
    container = new ChainContainer(orientation, max, install, uninstall, chains, program.getHash());
  }

  private CommandChain visitUnInstall(MplProcess process) {
    process.accept(this);
    CommandChain chain = chains.get(0);
    chains.remove(0);
    return chain;
  }

  private void addBreakpointProcess(MplProgram program) {
    String hash = program.getHash();
    MplProcess process = new MplProcess("breakpoint");
    List<ChainPart> commands = new ArrayList<>();

    // Pause
    if (!options.hasOption(TRANSMITTER)) {
      commands.add(new MplCommand("/execute @e[tag=" + hash + "] ~ ~ ~ clone ~ ~ ~ ~ ~ ~ ~ ~1 ~"));
    }
    commands.add(new MplCommand("/tp @e[tag=" + hash + "] ~ ~1 ~"));
    if (!options.hasOption(TRANSMITTER)) {
      commands
          .add(new MplCommand("/execute @e[tag=" + hash + "] ~ ~ ~ blockdata ~ ~ ~ {Command:}"));
    }

    commands.add(new MplCommand(
        "tellraw @a [{\"text\":\"[tp to breakpoint]\",\"color\":\"gold\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/tp @p @e[name=breakpoint_NOTIFY,c=-1]\"}},{\"text\":\" \"},{\"text\":\"[continue program]\",\"color\":\"gold\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/execute @e[name=breakpoint_CONTINUE] ~ ~ ~ "
            + getStartCommand("~ ~ ~") + "\"}}]"));

    commands.add(new MplWaitfor("breakpoint_CONTINUE"));
    commands.add(new MplCommand("/kill @e[name=breakpoint_CONTINUE]"));

    // Unpause
    commands.add(
        new MplCommand("/execute @e[tag=" + hash + "] ~ ~ ~ clone ~ ~ ~ ~ ~ ~ ~ ~-1 ~ force move"));
    commands.add(new MplCommand("/tp @e[tag=" + hash + "] ~ ~-1 ~"));
    if (!options.hasOption(TRANSMITTER)) {
      commands.add(new MplCommand("/execute @e[tag=" + hash + "] ~ ~ ~ blockdata ~ ~ ~ {Command:"
          + getStopCommand("~ ~ ~") + "}"));
    }

    commands.add(new MplNotify("breakpoint"));

    process.setChainParts(commands);
    program.addProcess(process);
    process.accept(this);
  }

  @Override
  public void visitProcess(MplProcess process) {
    List<ChainPart> chainParts = process.getChainParts();
    commands = new ArrayList<>(chainParts.size());
    boolean containsSkip = containsHighlevelSkip(process);
    if (process.isRepeating()) {
      if (options.hasOption(TRANSMITTER)) {
        commands.add(new MplSkip());
      }
      if (process.getChainParts().isEmpty()) {
        process.add(new MplCommand(""));
      }
      ChainPart first = chainParts.get(0);
      try {
        if (containsSkip) {
          first.setMode(IMPULSE);
        } else {
          first.setMode(REPEAT);
        }
        first.setNeedsRedstone(true);
      } catch (IllegalModifierException ex) {
        throw new IllegalStateException(ex.getMessage(), ex);
      }
    } else {
      addTransmitterReceiverCombo(false);
    }
    for (ChainPart chainPart : chainParts) {
      chainPart.accept(this);
    }
    if (process.isRepeating() && containsSkip) {
      addRestartBackref(commands.get(0), false);
    }
    chains.add(new CommandChain(process.getName(), commands));
  }

  private boolean containsHighlevelSkip(MplProcess process) {
    List<ChainPart> chainParts = process.getChainParts();
    for (ChainPart chainPart : chainParts) {
      if (chainPart instanceof MplWaitfor || chainPart instanceof MplIntercept
          || chainPart instanceof MplBreakpoint) {
        return true;
      }
    }
    return false;
  }

  protected void visitPossibleInvert(ModifiableChainPart chainPart) {
    if (chainPart.getConditional() == Conditional.INVERT) {
      Dependable previous = chainPart.getPrevious();
      checkState(previous != null,
          "Cannot invert ChainPart; no previous command found for " + chainPart);
      commands.add(new InvertingCommand(previous.getModeForInverting()));
    }
  }

  @Override
  public void visitCommand(MplCommand command) {
    visitPossibleInvert(command);

    String cmd = command.getCommand();
    commands.add(new Command(cmd, command));
  }

  @Override
  public void visitStart(MplStart start) {
    visitPossibleInvert(start);

    String process = start.getProcess();
    String command = "execute @e[name=" + process + "] ~ ~ ~ " + getStartCommand("~ ~ ~");
    commands.add(new Command(command, start));
  }

  @Override
  public void visitStop(MplStop stop) {
    visitPossibleInvert(stop);

    String process = stop.getProcess();
    String command = "execute @e[name=" + process + "] ~ ~ ~ " + getStopCommand("~ ~ ~");
    commands.add(new Command(command, stop));
  }

  @Override
  public void visitWaitfor(MplWaitfor waitfor) {
    ReferencingCommand summon = new ReferencingCommand("summon ArmorStand " + REF + " {CustomName:"
        + waitfor.getEvent() + ",NoGravity:1b,Invisible:1b,Invulnerable:1b,Marker:1b}");

    if (waitfor.getConditional() == UNCONDITIONAL) {
      summon.setRelative(1);
      commands.add(summon);
    } else {
      summon.setConditional(true);
      ReferencingCommand jump = new ReferencingCommand(getStartCommand(REF), true);
      if (waitfor.getConditional() == CONDITIONAL) {
        summon.setRelative(3);
        jump.setRelative(1);
        commands.add(summon);
        commands.add(new InvertingCommand(CHAIN));
        commands.add(jump);
      } else { // conditional == INVERT
        jump.setRelative(3);
        summon.setRelative(1);
        commands.add(jump);
        commands.add(new InvertingCommand(CHAIN));
        commands.add(summon);
      }
    }
    addTransmitterReceiverCombo(false);
  }

  @Override
  public void visitNotify(MplNotify notify) {
    visitPossibleInvert(notify);

    String process = notify.getProcess();
    boolean conditional = notify.isConditional();
    commands.add(new InternalCommand(
        "execute @e[name=" + process + NOTIFY + "] ~ ~ ~ " + getStartCommand("~ ~ ~"),
        conditional));
    commands.add(new Command("kill @e[name=" + process + NOTIFY + "]", conditional));
  }

  @Override
  public void visitIntercept(MplIntercept intercept) {
    String event = intercept.getEvent();
    boolean conditional = intercept.isConditional();

    InternalCommand entitydata = new InternalCommand(
        "entitydata @e[name=" + event + "] {CustomName:" + event + INTERCEPTED + "}", conditional);

    ReferencingCommand summon = new ReferencingCommand("summon ArmorStand " + REF + " {CustomName:"
        + event + ",NoGravity:1b,Invisible:1b,Invulnerable:1b,Marker:1b}", conditional);


    if (intercept.getConditional() == UNCONDITIONAL) {
      summon.setRelative(1);
      commands.add(entitydata);
      commands.add(summon);
    } else {
      ReferencingCommand jump = new ReferencingCommand(getStartCommand(REF), true);
      if (intercept.getConditional() == CONDITIONAL) {
        summon.setRelative(3);
        jump.setRelative(1);
        commands.add(entitydata);
        commands.add(summon);
        commands.add(new InvertingCommand(CHAIN));
        commands.add(jump);
      } else { // conditional == INVERT
        jump.setRelative(4);
        summon.setRelative(1);
        commands.add(jump);
        commands.add(new InvertingCommand(CHAIN));
        commands.add(entitydata);
        commands.add(summon);
      }
    }
    addTransmitterReceiverCombo(false);
    commands.add(new InternalCommand("kill @e[name=" + event + ",r=2]"));
    commands.add(new InternalCommand(
        "entitydata @e[name=" + event + INTERCEPTED + "] {CustomName:" + event + "}"));
  }

  @Override
  public void visitBreakpoint(MplBreakpoint breakpoint) {
    if (!options.hasOption(DEBUG)) {
      return;
    }
    addBreakpointProcess = true;

    visitPossibleInvert(breakpoint);

    boolean cond = breakpoint.isConditional();
    commands.add(new InternalCommand("say " + breakpoint.getMessage(), cond));

    ModifierBuffer modifier = new ModifierBuffer();
    modifier.setConditional(cond ? CONDITIONAL : UNCONDITIONAL);
    visitStart(new MplStart("breakpoint", modifier));
    visitWaitfor(new MplWaitfor("breakpoint" + NOTIFY, modifier));
  }

  @Override
  public void visitSkip(MplSkip skip) {
    commands.add(skip);
  }

  @RequiredArgsConstructor
  @Getter
  @Setter
  private static class IfNestingLayer {
    private final boolean not;
    private final @Nonnull InternalCommand ref;
    private boolean inElse;
  }

  private Deque<IfNestingLayer> ifNestingLayers = new ArrayDeque<>();

  @Override
  public void visitIf(MplIf mplIf) {
    String condition = mplIf.getCondition();
    InternalCommand ref = new InternalCommand(condition, mplIf);
    commands.add(ref);
    if (needsNormalizer(mplIf)) {
      ref = new NormalizingCommand();
      commands.add(ref);
    }
    IfNestingLayer layer = new IfNestingLayer(mplIf.isNot(), ref);
    ifNestingLayers.push(layer);

    // then
    layer.setInElse(false);
    Deque<ChainPart> thenParts = mplIf.getThenParts();
    boolean emptyThen = thenParts.isEmpty();
    if (!mplIf.isNot() && !emptyThen) {
      // First then does not need a reference
      addAsConditional(thenParts.pop());
    }
    addAllWithRef(thenParts);

    // else
    layer.setInElse(true);
    Deque<ChainPart> elseParts = mplIf.getElseParts();
    boolean emptyElse = elseParts.isEmpty();
    if (mplIf.isNot() && emptyThen && !emptyElse) {
      // First else does not need a reference, if there is no then part
      addAsConditional(elseParts.pop());
    }
    addAllWithRef(elseParts);

    ifNestingLayers.pop();
  }

  private void addAllWithRef(Iterable<ChainPart> chainParts) {
    for (ChainPart chainPart : chainParts) {
      addWithRef(cast(chainPart));
    }
  }

  private void addWithRef(ModifiableChainPart chainPart) {
    visitPossibleInvert(chainPart);
    if (chainPart.getConditional() != CONDITIONAL) {
      addConditionReferences(chainPart);
    }
    addAsConditional(chainPart);
  }

  /**
   * Add's all references to required {@link MplIf}s. If the chainPart depends on the parent's
   * failure a reference to the grandparent is also added. This method is recursive and will add
   * parent references, until the root is reached or until a layer depends on it's parent's success
   * rather that failure.
   */
  private void addConditionReferences(ModifiableChainPart chainPart) {
    Deque<IfNestingLayer> requiredReferences = new ArrayDeque<>();
    for (IfNestingLayer layer : ifNestingLayers) {
      requiredReferences.push(layer);
      boolean dependingOnFailure = layer.isNot() ^ layer.isInElse();
      if (!dependingOnFailure) {
        break;
      }
    }
    if (chainPart.getConditional() == UNCONDITIONAL) {
      IfNestingLayer first = requiredReferences.pop();
      commands.add(getConditionReference(first));
    }
    for (IfNestingLayer layer : requiredReferences) {
      ReferencingTestforSuccessCommand ref = getConditionReference(layer);
      ref.setConditional(true);
      commands.add(ref);
    }
  }

  private ReferencingTestforSuccessCommand getConditionReference(IfNestingLayer layer) {
    InternalCommand ref = layer.getRef();
    int relative = getCountToRef(ref);
    boolean dependingOnFailure = layer.isNot() ^ layer.isInElse();
    return new ReferencingTestforSuccessCommand(relative, ref.getMode(), !dependingOnFailure);
  }

  private void addAsConditional(ChainPart chainPart) {
    cast(chainPart).setConditional(CONDITIONAL);
    chainPart.accept(this);
  }

  private static ModifiableChainPart cast(ChainPart chainPart) {
    try {
      return (ModifiableChainPart) chainPart;
    } catch (ClassCastException ex) {
      throw new IllegalStateException("If cannot contain skip", ex);
    }
  }

  public static boolean needsNormalizer(MplIf mplIf) {
    if (!mplIf.isNot()) {
      return containsConditionReferenceIgnoringFirstNonIf(mplIf.getThenParts());
    } else {
      if (!mplIf.getThenParts().isEmpty()) {
        if (!mplIf.getElseParts().isEmpty())
          return true;
        else
          return false;
      }
      return containsConditionReferenceIgnoringFirstNonIf(mplIf.getElseParts());
    }
  }

  private static boolean containsConditionReferenceIgnoringFirstNonIf(
      Iterable<ChainPart> iterable) {
    Iterator<ChainPart> it = iterable.iterator();
    if (it.hasNext()) {
      ChainPart first = it.next(); // Ignore the first element.
      if (first instanceof MplIf) {
        it = iterable.iterator(); // Only if it is not a nested if
      }
    }
    return containsConditionReference(it);
  }

  private static boolean containsConditionReference(Iterator<ChainPart> it) {
    while (it.hasNext()) {
      ChainPart chainPart = it.next();
      if (chainPart instanceof MplIf) {
        if (needsParentNormalizer((MplIf) chainPart)) {
          return true;
        }
      } else if (chainPart instanceof ModifiableChainPart) {
        ModifiableChainPart cp = (ModifiableChainPart) chainPart;
        if (!cp.isConditional()) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean needsParentNormalizer(MplIf mplIf) {
    if (mplIf.isNot()) {
      return containsConditionReference(mplIf.getThenParts().iterator());
    } else {
      return containsConditionReference(mplIf.getElseParts().iterator());
    }
  }

  @Override
  public void visitWhile(MplWhile mplWhile) {
    String condition = mplWhile.getCondition();
    // TODO: mit null condition umgehen können (repeat ohne condition)

    Deque<ChainPart> chainParts = mplWhile.getChainParts();
    if (chainParts.isEmpty()) {
      chainParts.add(new MplCommand(""));
    }

    if (mplWhile.isTrailing()) {
      commands.add(newReferencingStartCommand(false, 1));
    } else {
      commands.add(new Command(condition));
      int offset = options.hasOption(TRANSMITTER) ? 1 : 0;
      if (!mplWhile.isNot()) {
        commands.add(newReferencingStartCommand(true, 3));
        commands.add(new InvertingCommand(CHAIN));
        commands.add(newReferencingStartCommand(true, chainParts.size() + 7 + offset));
      } else {
        commands.add(newReferencingStartCommand(true, chainParts.size() + 9 + offset));
        commands.add(new InvertingCommand(CHAIN));
        commands.add(newReferencingStartCommand(true, 1));
      }
    }
    try {
      ChainPart first = chainParts.peek();
      first.setMode(IMPULSE);
      first.setNeedsRedstone(true);
    } catch (IllegalModifierException ex) {
      throw new IllegalStateException("While cannot contain skip", ex);
    }
    int firstIndex = commands.size();
    if (options.hasOption(TRANSMITTER)) {
      commands.add(new MplSkip(true));
    }
    for (ChainPart chainPart : chainParts) {
      chainPart.accept(this);
    }
    ChainLink entryPoint = commands.get(firstIndex);

    commands.add(new Command(condition));

    ReferencingCommand _continue = new ReferencingCommand(getStartCommand(REF), true);
    InvertingCommand invert = new InvertingCommand(CHAIN);
    ReferencingCommand stop = new ReferencingCommand(getStopCommand(REF), true);

    if (!mplWhile.isNot()) {
      addRestartBackref(entryPoint, true);
      commands.add(invert);

      _continue.setRelative(2);
      commands.add(_continue);

      stop.setRelative(getCountToRef(entryPoint));
      commands.add(stop);
    } else {
      _continue.setRelative(5);
      commands.add(_continue);

      stop.setRelative(getCountToRef(entryPoint));
      commands.add(stop);

      commands.add(invert);
      addRestartBackref(entryPoint, true);
    }
    addTransmitterReceiverCombo(true);
  }

}
