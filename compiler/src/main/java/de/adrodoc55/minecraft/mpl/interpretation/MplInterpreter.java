/*
 * Minecraft Programming Language (MPL): A language for easy development of command block
 * applications including an IDE.
 *
 * © Copyright (C) 2016 Adrodoc55
 *
 * This file is part of MPL.
 *
 * MPL is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MPL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MPL. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 *
 *
 * Minecraft Programming Language (MPL): Eine Sprache für die einfache Entwicklung von Commandoblock
 * Anwendungen, inklusive einer IDE.
 *
 * © Copyright (C) 2016 Adrodoc55
 *
 * Diese Datei ist Teil von MPL.
 *
 * MPL ist freie Software: Sie können diese unter den Bedingungen der GNU General Public License,
 * wie von der Free Software Foundation, Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
 * veröffentlichten Version, weiterverbreiten und/oder modifizieren.
 *
 * MPL wird in der Hoffnung, dass es nützlich sein wird, aber OHNE JEDE GEWÄHRLEISTUNG,
 * bereitgestellt; sogar ohne die implizite Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN
 * BESTIMMTEN ZWECK. Siehe die GNU General Public License für weitere Details.
 *
 * Sie sollten eine Kopie der GNU General Public License zusammen mit MPL erhalten haben. Wenn
 * nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.adrodoc55.minecraft.mpl.interpretation;

import static de.adrodoc55.minecraft.mpl.ast.chainparts.MplNotify.NOTIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

import de.adrodoc55.commons.FileUtils;
import de.adrodoc55.minecraft.coordinate.Orientation3D;
import de.adrodoc55.minecraft.mpl.antlr.MplBaseListener;
import de.adrodoc55.minecraft.mpl.antlr.MplLexer;
import de.adrodoc55.minecraft.mpl.antlr.MplParser;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.AutoContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.BreakDeclarationContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.BreakpointContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.CommandContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.ConditionalContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.ContinueDeclarationContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.ElseDeclarationContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.FileContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.IfDeclarationContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.ImportDeclarationContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.IncludeContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.InstallContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.InterceptContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.ModusContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.MplCommandContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.NotifyDeclarationContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.OrientationContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.ProcessContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.ProjectContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.ScriptFileContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.SkipDeclarationContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.StartContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.StopContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.ThenContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.UninstallContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.WaitforContext;
import de.adrodoc55.minecraft.mpl.antlr.MplParser.WhileDeclarationContext;
import de.adrodoc55.minecraft.mpl.ast.chainparts.ChainPart;
import de.adrodoc55.minecraft.mpl.ast.chainparts.ModifiableChainPart;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplBreakpoint;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplCommand;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplIf;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplIntercept;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplNotify;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplStart;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplStop;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplWaitfor;
import de.adrodoc55.minecraft.mpl.ast.chainparts.loop.MplBreak;
import de.adrodoc55.minecraft.mpl.ast.chainparts.loop.MplContinue;
import de.adrodoc55.minecraft.mpl.ast.chainparts.loop.MplWhile;
import de.adrodoc55.minecraft.mpl.ast.chainparts.program.MplProcess;
import de.adrodoc55.minecraft.mpl.ast.chainparts.program.MplProgram;
import de.adrodoc55.minecraft.mpl.commands.Conditional;
import de.adrodoc55.minecraft.mpl.commands.Mode;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.MplSkip;
import de.adrodoc55.minecraft.mpl.compilation.CompilerException;
import de.adrodoc55.minecraft.mpl.compilation.MplSource;
import de.adrodoc55.minecraft.mpl.interpretation.ChainPartBuffer.ChainPartBufferImpl;

/**
 * @author Adrodoc55
 */
public class MplInterpreter extends MplBaseListener {

  public static MplInterpreter interpret(File programFile) throws IOException {
    MplInterpreter interpreter = new MplInterpreter(programFile);
    FileContext ctx = interpreter.parse(programFile);
    if (interpreter.getProgram().getExceptions().isEmpty()) {
      new ParseTreeWalker().walk(interpreter, ctx);
    }
    return interpreter;
  }

  private FileContext parse(File programFile) throws IOException {
    byte[] bytes = Files.readAllBytes(programFile.toPath());
    ANTLRInputStream input = new ANTLRInputStream(FileUtils.toUnixLineEnding(new String(bytes)));
    MplLexer lexer = new MplLexer(input);
    TokenStream tokens = new CommonTokenStream(lexer);
    MplParser parser = new MplParser(tokens);
    parser.removeErrorListeners();
    parser.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object token, int line,
          int charPositionInLine, String message, RecognitionException cause) {
        MplSource source = toSource((Token) token);
        addException(new CompilerException(source, message));
      }
    });
    FileContext context = parser.file();
    // Trees.inspect(context, parser);
    return context;
  }

  private final File programFile;
  private final List<String> lines;
  private final ListMultimap<String, Include> includes = LinkedListMultimap.create();
  private final Set<File> imports = new HashSet<>();

  private MplInterpreter(File programFile) throws IOException {
    this.programFile = programFile;
    lines = Files.readAllLines(programFile.toPath());
    // FIXME was sinnvolleres als null reinstecken
    addFileImport(null, programFile.getParentFile());
  }

  public File getProgramFile() {
    return programFile;
  }

  private final MplProgram program = new MplProgram();

  public MplProgram getProgram() {
    return program;
  }

  private void addException(CompilerException ex1) {
    program.getExceptions().add(ex1);
  }

  /**
   * Returns the mapping of process names to includes required by that process. A key of null
   * indicates that this include is not required by a specific process, but by an explicit include
   * of a project.
   *
   * @return the mapping of process names
   */
  public ListMultimap<String, Include> getIncludes() {
    return includes;
  }

  // ----------------------------------------------------------------------------------------------------

  public MplSource toSource(@Nullable Token token) {
    String line = token != null ? lines.get(token.getLine() - 1) : null;
    return new MplSource(programFile, token, line);
  }

  @Override
  public void exitFile(FileContext ctx) {
    if (program.getOrientation() == null) {
      program.setOrientation(new Orientation3D());
    }
  }

  @Override
  public void enterImportDeclaration(ImportDeclarationContext ctx) {
    Token token = ctx.STRING().getSymbol();
    String importPath = MplLexerUtils.getContainedString(token);
    File file = new File(programFile.getParentFile(), importPath);
    addFileImport(ctx, file);
  }

  @Override
  public void enterProject(ProjectContext ctx) {
    Token oldToken = program.getToken();
    Token newToken = ctx.PROJECT().getSymbol();
    if (oldToken != null) {
      String message = "A file can only contain a single project";
      MplSource oldSource = toSource(oldToken);
      addException(new CompilerException(oldSource, message));
      MplSource newSource = toSource(newToken);
      addException(new CompilerException(newSource, message));
      return;
    }
    String name = ctx.IDENTIFIER().getText();
    program.setName(name);
    program.setToken(newToken);
  }

  @Override
  public void enterOrientation(OrientationContext ctx) {
    String def = MplLexerUtils.getContainedString(ctx.STRING().getSymbol());

    Token newToken = ctx.ORIENTATION().getSymbol();

    Orientation3D oldOrientation = program.getOrientation();
    if (oldOrientation != null) {
      String type = program.isScript() ? "script" : "project";
      String message = "A " + type + " can only have a single orientation";
      Token oldToken = oldOrientation.getToken();
      MplSource oldSource = toSource(oldToken);
      addException(new CompilerException(oldSource, message));
      MplSource newSource = toSource(newToken);
      addException(new CompilerException(newSource, message));
      return;
    }

    Orientation3D newOrientation = new Orientation3D(def, newToken);
    program.setOrientation(newOrientation);
  }

  @Override
  public void enterInclude(IncludeContext ctx) {
    String includePath = MplLexerUtils.getContainedString(ctx.STRING().getSymbol());
    Token token = ctx.STRING().getSymbol();
    File file = new File(programFile.getParentFile(), includePath);
    List<File> files = new ArrayList<File>();
    if (!addFile(files, file, token)) {
      return;
    }
    MplSource source = toSource(token);
    Include include = new Include(source, files);
    if (includes.get(null).contains(include)) {
      addException(new CompilerException(source, "Duplicate include"));
      return;
    }
    includes.put(null, include);
  }

  /**
   * Adds the file to the list of imports that will be used to search processes. If the file is a
   * directory all direct subfiles will be added, this is not recursive.
   *
   * @param ctx the token context that this import originated from
   * @param file the file to import
   */
  private void addFileImport(ImportDeclarationContext ctx, File file) {
    Token token = ctx != null ? ctx.STRING().getSymbol() : null;
    if (imports.contains(file)) {
      MplSource source = toSource(token);
      addException(new CompilerException(source, "Duplicate import"));
      return;
    }
    addFile(imports, file, token);
  }

  /**
   * Adds the File to the specified Collection. If the File is a Directory all relevant children are
   * added instead. If any Problem occurs, an Exception will be added to the exceptions-List.
   *
   * @param files the Collection to add to
   * @param file the File
   * @param token the Token to display in potential Exceptions
   * @return true if something was added to the Collection, false otherwise
   */
  private boolean addFile(Collection<File> files, File file, @Nullable Token token) {
    if (file.isFile()) {
      files.add(file);
      return true;
    } else if (file.isDirectory()) {
      boolean added = false;
      for (File f : file.listFiles()) {
        if (f.isFile() && (f.equals(programFile) || f.getName().endsWith(".mpl"))) {
          files.add(f);
          added = true;
        }
      }
      return added;
    } else if (!file.exists()) {
      String path = FileUtils.getCanonicalPath(file);
      MplSource source = toSource(token);
      addException(new CompilerException(source, "Could not find '" + path + "'"));
      return false;
    } else {
      MplSource source = toSource(token);
      addException(new CompilerException(source,
          "Can only import Files and Directories, not: '" + file + "'"));
      return false;
    }
  }

  private final Deque<ChainPartBuffer> chainBufferStack = new LinkedList<>();
  private ChainPartBuffer chainBuffer;
  // private IfBuffer ifBuffer;

  private void newChainBuffer() {
    chainBufferStack.push(chainBuffer);
    chainBuffer = new ChainPartBufferImpl();
    // this.ifBuffer = new IfBuffer(chainBuffer);
  }

  private void popChainBuffer() {
    // ifBuffer is not recovered, because it is currently not required
    chainBuffer = chainBufferStack.poll();
    // ifBuffer = null;
  }

  private MplProcess process;

  @Override
  public void enterInstall(InstallContext ctx) {
    newChainBuffer();
  }

  @Override
  public void exitInstall(InstallContext ctx) {
    MplProcess install = program.getInstall();
    if (install == null) {
      install = new MplProcess("install");
      program.setInstall(install);
    }
    install.addAll(chainBuffer.getChainParts());

    popChainBuffer();
  }

  @Override
  public void enterUninstall(UninstallContext ctx) {
    newChainBuffer();
  }

  @Override
  public void exitUninstall(UninstallContext ctx) {
    MplProcess install = program.getUninstall();
    if (install == null) {
      install = new MplProcess("uninstall");
      program.setUninstall(install);
    }
    install.addAll(chainBuffer.getChainParts());

    popChainBuffer();
  }

  @Override
  public void enterScriptFile(ScriptFileContext ctx) {
    program.setScript(true);
    newChainBuffer();
  }

  @Override
  public void exitScriptFile(ScriptFileContext ctx) {
    process = new MplProcess();
    Deque<ChainPart> chainParts = chainBuffer.getChainParts();
    process.setChainParts(chainParts);
    program.addProcess(process);
    process = null;

    popChainBuffer();
  }

  @Override
  public void enterProcess(ProcessContext ctx) {
    String name = ctx.IDENTIFIER().getText();
    boolean repeat = ctx.REPEAT() != null;
    MplSource source = toSource(ctx.IDENTIFIER().getSymbol());
    process = new MplProcess(name, repeat, source);

    newChainBuffer();
  }

  @Override
  public void exitProcess(ProcessContext ctx) {
    Deque<ChainPart> chainParts = chainBuffer.getChainParts();
    process.setChainParts(chainParts);
    program.addProcess(process);
    process = null;

    popChainBuffer();
  }

  private ModifierBuffer modifierBuffer;

  @Override
  public void enterMplCommand(MplCommandContext ctx) {
    modifierBuffer = new ModifierBuffer();
  }

  @Override
  public void exitMplCommand(MplCommandContext ctx) {
    modifierBuffer = null;
  }

  @Override
  public void enterModus(ModusContext ctx) {
    if (ctx.IMPULSE() != null) {
      modifierBuffer.setModeToken(ctx.IMPULSE().getSymbol());
      modifierBuffer.setMode(Mode.IMPULSE);
      return;
    }
    if (ctx.CHAIN() != null) {
      modifierBuffer.setModeToken(ctx.CHAIN().getSymbol());
      modifierBuffer.setMode(Mode.CHAIN);
      return;
    }
    if (ctx.REPEAT() != null) {
      modifierBuffer.setModeToken(ctx.REPEAT().getSymbol());
      modifierBuffer.setMode(Mode.REPEAT);
      return;
    }
  }

  @Override
  public void enterConditional(ConditionalContext ctx) {
    if (ctx.UNCONDITIONAL() != null) {
      modifierBuffer.setConditionalToken(ctx.UNCONDITIONAL().getSymbol());
      modifierBuffer.setConditional(Conditional.UNCONDITIONAL);
      return;
    }
    if (ctx.CONDITIONAL() != null) {
      modifierBuffer.setConditionalToken(ctx.CONDITIONAL().getSymbol());
      modifierBuffer.setConditional(Conditional.CONDITIONAL);
      return;
    }
    if (ctx.INVERT() != null) {
      modifierBuffer.setConditionalToken(ctx.INVERT().getSymbol());
      modifierBuffer.setConditional(Conditional.INVERT);
      return;
    }
  }

  @Override
  public void enterAuto(AutoContext ctx) {
    if (ctx.ALWAYS_ACTIVE() != null) {
      modifierBuffer.setNeedsRedstoneToken(ctx.ALWAYS_ACTIVE().getSymbol());
      modifierBuffer.setNeedsRedstone(false);
      return;
    }
    if (ctx.NEEDS_REDSTONE() != null) {
      modifierBuffer.setNeedsRedstoneToken(ctx.NEEDS_REDSTONE().getSymbol());
      modifierBuffer.setNeedsRedstone(true);
      return;
    }
  }

  /**
   * Check that the given {@link Token} is null. If it is not null add a {@link CompilerException}.
   *
   * @param part - name of the {@link ChainPart} that may not have this modifier
   * @param token to check
   */
  private void checkNoModifier(String part, Token token) {
    if (token == null) {
      return;
    }
    MplSource source = toSource(token);
    addException(new CompilerException(source, "Illegal modifier for " + part
        + "; only unconditional, conditional and invert are permitted"));
  }

  private void addModifiableChainPart(ModifiableChainPart chainPart) {
    Conditional conditional = chainPart.getConditional();
    if (conditional == Conditional.UNCONDITIONAL) {
      chainBuffer.add(chainPart);
      return;
    }
    ChainPart prev = chainBuffer.getChainParts().peekLast();
    if (prev == null) {
      Token token = modifierBuffer.getConditionalToken();
      MplSource source = toSource(token);
      addException(
          new CompilerException(source, "The first part of a chain must be unconditional"));
      return;
    }
    if (prev.canBeDependedOn()) {
      chainPart.setPrevious(prev);
      chainBuffer.add(chainPart);
    } else {
      Token token = modifierBuffer.getConditionalToken();
      MplSource source = toSource(token);
      addException(new CompilerException(source,
          conditional.name().toLowerCase() + " cannot depend on " + prev.getName()));
    }
  }

  @Override
  public void enterCommand(CommandContext ctx) {
    String commandString = ctx.COMMAND().getText();
    MplCommand command = new MplCommand(commandString, modifierBuffer);
    addModifiableChainPart(command);
  }

  private String lastStartIdentifier;

  @Override
  public void enterStart(StartContext ctx) {
    TerminalNode identifier = ctx.IDENTIFIER();
    String process = identifier.getText();
    MplStart start = new MplStart(process, modifierBuffer);
    addModifiableChainPart(start);

    checkNoModifier(start.getName(), modifierBuffer.getModeToken());
    checkNoModifier(start.getName(), modifierBuffer.getNeedsRedstoneToken());

    lastStartIdentifier = process;
    if (program.isScript()) {
      return;
    }

    String srcProcess = this.process != null ? this.process.getName() : null;
    Token token = identifier.getSymbol();
    MplSource source = toSource(token);
    Include include = new Include(source, process, imports);
    includes.put(srcProcess, include);
  }

  @Override
  public void enterStop(StopContext ctx) {
    Token token = ctx.STOP().getSymbol();
    MplSource source = toSource(token);

    String process;
    if (ctx.IDENTIFIER() != null) {
      process = ctx.IDENTIFIER().getText();
    } else if (this.process != null) {
      if (this.process.isRepeating()) {
        process = this.process.getName();
      } else {
        addException(new CompilerException(source, "An impulse process cannot be stopped"));
        return;
      }
    } else {
      addException(new CompilerException(source, "Missing identifier"));
      return;
    }

    MplStop stop = new MplStop(process, modifierBuffer);
    addModifiableChainPart(stop);

    checkNoModifier(stop.getName(), modifierBuffer.getModeToken());
    checkNoModifier(stop.getName(), modifierBuffer.getNeedsRedstoneToken());
  }

  @Override
  public void enterWaitfor(WaitforContext ctx) {
    TerminalNode identifier = ctx.IDENTIFIER();
    String event;
    if (identifier != null) {
      event = identifier.getText();
      if (ctx.NOTIFY() != null) {
        event += NOTIFY;
      }
    } else if (lastStartIdentifier != null) {
      event = lastStartIdentifier += NOTIFY;
      lastStartIdentifier = null;
    } else {
      Token token = ctx.WAITFOR().getSymbol();
      MplSource source = toSource(token);
      addException(new CompilerException(source,
          "Missing identifier; no previous start was found to wait for"));
      return;
    }
    MplWaitfor waitfor = new MplWaitfor(event, modifierBuffer);
    addModifiableChainPart(waitfor);

    checkNoModifier(waitfor.getName(), modifierBuffer.getModeToken());
    checkNoModifier(waitfor.getName(), modifierBuffer.getNeedsRedstoneToken());
  }

  @Override
  public void enterNotifyDeclaration(NotifyDeclarationContext ctx) {
    if (this.process == null) {
      Token token = ctx.NOTIFY().getSymbol();
      MplSource source = toSource(token);
      addException(new CompilerException(source, "notify can only be used in a process"));
      return;
    }
    String process = this.process.getName();
    MplNotify notify = new MplNotify(process, modifierBuffer);
    addModifiableChainPart(notify);

    checkNoModifier(notify.getName(), modifierBuffer.getModeToken());
    checkNoModifier(notify.getName(), modifierBuffer.getNeedsRedstoneToken());
  }

  @Override
  public void enterIntercept(InterceptContext ctx) {
    TerminalNode identifier = ctx.IDENTIFIER();
    String process = identifier.getText();
    MplIntercept intercept = new MplIntercept(process, modifierBuffer);
    addModifiableChainPart(intercept);

    checkNoModifier(intercept.getName(), modifierBuffer.getModeToken());
    checkNoModifier(intercept.getName(), modifierBuffer.getNeedsRedstoneToken());
  }

  @Override
  public void enterBreakpoint(BreakpointContext ctx) {
    int line = ctx.BREAKPOINT().getSymbol().getLine();
    String source = programFile.getName() + " : line " + line;
    MplBreakpoint breakpoint = new MplBreakpoint(source, modifierBuffer);
    addModifiableChainPart(breakpoint);

    checkNoModifier(breakpoint.getName(), modifierBuffer.getModeToken());
    checkNoModifier(breakpoint.getName(), modifierBuffer.getNeedsRedstoneToken());

    // getProject().setHasBreakpoint(true);
  }

  @Override
  public void enterSkipDeclaration(SkipDeclarationContext ctx) {
    if (process != null && process.isRepeating() && chainBuffer.getChainParts().isEmpty()) {
      MplSource source = toSource(ctx.SKIP_TOKEN().getSymbol());
      addException(
          new CompilerException(source, "skip cannot be the first command of a repeating process"));
      return;
    }
    chainBuffer.add(new MplSkip());
  }

  @Override
  public void enterIfDeclaration(IfDeclarationContext ctx) {
    boolean not = ctx.NOT() != null;
    String condition = ctx.COMMAND().getText();
    chainBuffer = new MplIf(chainBuffer, not, condition);
  }

  @Override
  public void enterThen(ThenContext ctx) {
    ((MplIf) chainBuffer).enterThen();
  }

  @Override
  public void enterElseDeclaration(ElseDeclarationContext ctx) {
    ((MplIf) chainBuffer).enterElse();
  }

  @Override
  public void exitIfDeclaration(IfDeclarationContext ctx) {
    MplIf mplIf = (MplIf) chainBuffer;
    chainBuffer = mplIf.exit();
    chainBuffer.add(mplIf);
  }

  private Deque<MplWhile> loops = new ArrayDeque<>();

  @Override
  public void enterWhileDeclaration(WhileDeclarationContext ctx) {
    TerminalNode identifier = ctx.IDENTIFIER();
    String label = identifier != null ? identifier.getText() : null;
    boolean not = ctx.NOT() != null;
    boolean trailing = ctx.DO() != null;
    TerminalNode command = ctx.COMMAND();
    String condition = command != null ? command.getText() : null;

    MplWhile mplWhile = new MplWhile(chainBuffer, label, not, trailing, condition);
    loops.push(mplWhile);
    chainBuffer = mplWhile;
  }

  @Override
  public void exitWhileDeclaration(WhileDeclarationContext ctx) {
    loops.pop();
    MplWhile mplWhile = (MplWhile) chainBuffer;
    chainBuffer = mplWhile.exit();
    chainBuffer.add(mplWhile);
  }

  @Override
  public void enterBreakDeclaration(BreakDeclarationContext ctx) {
    TerminalNode identifier = ctx.IDENTIFIER();
    String label = identifier != null ? identifier.getText() : null;

    MplWhile loop;
    if (label == null) {
      MplSource source = toSource(ctx.BREAK().getSymbol());
      loop = findParentLoop(source);
    } else {
      MplSource source = toSource(ctx.IDENTIFIER().getSymbol());
      loop = findParentLoop(label, source);
    }
    if (loop == null) {
      return;
    }

    MplBreak mplBreak = new MplBreak(label, loop, modifierBuffer);
    addModifiableChainPart(mplBreak);

    checkNoModifier(mplBreak.getName(), modifierBuffer.getModeToken());
    checkNoModifier(mplBreak.getName(), modifierBuffer.getNeedsRedstoneToken());
  }

  @Override
  public void enterContinueDeclaration(ContinueDeclarationContext ctx) {
    TerminalNode identifier = ctx.IDENTIFIER();
    String label = identifier != null ? identifier.getText() : null;

    MplWhile loop;
    if (label == null) {
      MplSource source = toSource(ctx.CONTINUE().getSymbol());
      loop = findParentLoop(source);
    } else {
      MplSource source = toSource(ctx.IDENTIFIER().getSymbol());
      loop = findParentLoop(label, source);
    }
    if (loop == null) {
      return;
    }

    MplContinue mplContinue = new MplContinue(label, loop, modifierBuffer);
    addModifiableChainPart(mplContinue);

    checkNoModifier(mplContinue.getName(), modifierBuffer.getModeToken());
    checkNoModifier(mplContinue.getName(), modifierBuffer.getNeedsRedstoneToken());
  }

  public MplWhile findParentLoop(MplSource source) {
    MplWhile loop;
    loop = loops.peek();
    if (loop == null) {
      addException(
          new CompilerException(source, source.token.getText() + " can only be used in a loop"));
    }
    return loop;
  }

  public MplWhile findParentLoop(String label, MplSource source) {
    MplWhile loop = null;
    for (MplWhile mplWhile : loops) {
      if (label.equals(mplWhile.getLabel())) {
        loop = mplWhile;
        break;
      }
    }
    if (loop == null) {
      addException(new CompilerException(source, "Missing label " + label));
    }
    return loop;
  }

}
