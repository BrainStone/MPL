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
package de.adrodoc55.minecraft.mpl.ast;

import static de.adrodoc55.TestBase.$Enum;
import static de.adrodoc55.TestBase.$boolean;
import static de.adrodoc55.TestBase.$oneOf;
import static de.adrodoc55.TestBase.listOf;
import static de.adrodoc55.TestBase.some;
import static de.adrodoc55.minecraft.mpl.MplTestBase.$MplBreak;
import static de.adrodoc55.minecraft.mpl.MplTestBase.$MplBreakpoint;
import static de.adrodoc55.minecraft.mpl.MplTestBase.$MplCommand;
import static de.adrodoc55.minecraft.mpl.MplTestBase.$MplContinue;
import static de.adrodoc55.minecraft.mpl.MplTestBase.$MplIf;
import static de.adrodoc55.minecraft.mpl.MplTestBase.$MplNotify;
import static de.adrodoc55.minecraft.mpl.MplTestBase.$MplProcess;
import static de.adrodoc55.minecraft.mpl.MplTestBase.$MplProgram;
import static de.adrodoc55.minecraft.mpl.MplTestBase.$MplSkip;
import static de.adrodoc55.minecraft.mpl.MplTestBase.$MplStart;
import static de.adrodoc55.minecraft.mpl.MplTestBase.$MplStop;
import static de.adrodoc55.minecraft.mpl.MplTestBase.$MplWaitfor;
import static de.adrodoc55.minecraft.mpl.MplTestBase.$MplWhile;
import static de.adrodoc55.minecraft.mpl.ast.chainparts.MplNotify.NOTIFY;
import static de.adrodoc55.minecraft.mpl.commands.Conditional.CONDITIONAL;
import static de.adrodoc55.minecraft.mpl.commands.Conditional.INVERT;
import static de.adrodoc55.minecraft.mpl.commands.Conditional.UNCONDITIONAL;
import static de.adrodoc55.minecraft.mpl.commands.Mode.CHAIN;
import static de.adrodoc55.minecraft.mpl.commands.Mode.IMPULSE;
import static de.adrodoc55.minecraft.mpl.commands.chainlinks.ReferencingCommand.REF;
import static de.adrodoc55.minecraft.mpl.compilation.CompilerOptions.CompilerOption.TRANSMITTER;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.adrodoc55.minecraft.mpl.ast.chainparts.Dependable;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplBreakpoint;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplCommand;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplIf;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplNotify;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplStart;
import de.adrodoc55.minecraft.mpl.ast.chainparts.MplStop;
import de.adrodoc55.minecraft.mpl.ast.chainparts.loop.MplBreak;
import de.adrodoc55.minecraft.mpl.ast.chainparts.loop.MplContinue;
import de.adrodoc55.minecraft.mpl.ast.chainparts.loop.MplWhile;
import de.adrodoc55.minecraft.mpl.ast.chainparts.program.MplProgram;
import de.adrodoc55.minecraft.mpl.chain.CommandChain;
import de.adrodoc55.minecraft.mpl.commands.Mode;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.ChainLink;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.Command;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.InternalCommand;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.InvertingCommand;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.MplSkip;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.NormalizingCommand;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.ReferencingCommand;
import de.adrodoc55.minecraft.mpl.commands.chainlinks.ReferencingTestforSuccessCommand;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class MplAstVisitorTest {

  protected MplAstVisitorImpl underTest;

  @Before
  public void before() {
    underTest = newUnderTest();
  }

  protected abstract MplAstVisitorImpl newUnderTest();

  protected abstract String getOnCommand(String ref);

  protected abstract String getOffCommand(String ref);

  // @formatter:off
  // ----------------------------------------------------------------------------------------------------
  //    ____   _                _
  //   / ___| | |_  __ _  _ __ | |_
  //   \___ \ | __|/ _` || '__|| __|
  //    ___) || |_| (_| || |   | |_
  //   |____/  \__|\__,_||_|    \__|
  //
  // ----------------------------------------------------------------------------------------------------
  // @formatter:on

  @Test
  public void test_unconditional_Start() {
    // given:
    MplStart mplStart = some($MplStart()//
        .withConditional(UNCONDITIONAL));
    Mode mode = mplStart.getMode();
    boolean needsRedstone = mplStart.getNeedsRedstone();

    // when:
    mplStart.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(
            "/execute @e[name=" + mplStart.getProcess() + "] ~ ~ ~ " + getOnCommand("~ ~ ~"), mode,
            false, needsRedstone));
  }

  @Test
  public void test_conditional_Start() {
    // given:
    MplStart mplStart = some($MplStart()//
        .withConditional(CONDITIONAL));
    Mode mode = mplStart.getMode();
    boolean needsRedstone = mplStart.getNeedsRedstone();

    // when:
    mplStart.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(
            "/execute @e[name=" + mplStart.getProcess() + "] ~ ~ ~ " + getOnCommand("~ ~ ~"), mode,
            true, needsRedstone));
  }

  @Test
  public void test_invert_Start() {
    // given:
    Mode modeForInverting = some($Enum(Mode.class));
    MplStart mplStart = some($MplStart()//
        .withConditional(INVERT)//
        .withPrevious(new Dependable() {
          @Override
          public boolean canBeDependedOn() {
            return true;
          }

          @Override
          public Mode getModeForInverting() {
            return modeForInverting;
          }
        }));
    Mode mode = mplStart.getMode();
    boolean needsRedstone = mplStart.getNeedsRedstone();

    // when:
    mplStart.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InvertingCommand(modeForInverting), //
        new InternalCommand(
            "/execute @e[name=" + mplStart.getProcess() + "] ~ ~ ~ " + getOnCommand("~ ~ ~"), mode,
            true, needsRedstone));
  }

  // @formatter:off
  // ----------------------------------------------------------------------------------------------------
  //    ____   _
  //   / ___| | |_  ___   _ __
  //   \___ \ | __|/ _ \ | '_ \
  //    ___) || |_| (_) || |_) |
  //   |____/  \__|\___/ | .__/
  //                     |_|
  // ----------------------------------------------------------------------------------------------------
  // @formatter:on

  @Test
  public void test_unconditional_Stop() {
    // given:
    MplStop mplStop = some($MplStop()//
        .withConditional(UNCONDITIONAL));
    Mode mode = mplStop.getMode();
    boolean needsRedstone = mplStop.getNeedsRedstone();

    // when:
    mplStop.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(
            "/execute @e[name=" + mplStop.getProcess() + "] ~ ~ ~ " + getOffCommand("~ ~ ~"), mode,
            false, needsRedstone));
  }

  @Test
  public void test_conditional_Stop() {
    // given:
    MplStop mplStop = some($MplStop()//
        .withConditional(CONDITIONAL));
    Mode mode = mplStop.getMode();
    boolean needsRedstone = mplStop.getNeedsRedstone();

    // when:
    mplStop.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(
            "/execute @e[name=" + mplStop.getProcess() + "] ~ ~ ~ " + getOffCommand("~ ~ ~"), mode,
            true, needsRedstone));
  }

  @Test
  public void test_invert_Stop() {
    // given:
    Mode modeForInvering = some($Enum(Mode.class));
    MplStop mplStop = some($MplStop()//
        .withConditional(INVERT)//
        .withPrevious(new Dependable() {
          @Override
          public boolean canBeDependedOn() {
            return true;
          }

          @Override
          public Mode getModeForInverting() {
            return modeForInvering;
          }
        }));
    Mode mode = mplStop.getMode();
    boolean needsRedstone = mplStop.getNeedsRedstone();

    // when:
    mplStop.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InvertingCommand(modeForInvering),
        new InternalCommand(
            "/execute @e[name=" + mplStop.getProcess() + "] ~ ~ ~ " + getOffCommand("~ ~ ~"), mode,
            true, needsRedstone));
  }

  // @formatter:off
  // ----------------------------------------------------------------------------------------------------
  //    _   _         _    _   __
  //   | \ | |  ___  | |_ (_) / _| _   _
  //   |  \| | / _ \ | __|| || |_ | | | |
  //   | |\  || (_) || |_ | ||  _|| |_| |
  //   |_| \_| \___/  \__||_||_|   \__, |
  //                               |___/
  // ----------------------------------------------------------------------------------------------------
  // @formatter:on

  @Test
  public void test_unconditional_Notify() {
    // given:
    MplNotify mplNotify = some($MplNotify()//
        .withConditional(UNCONDITIONAL));

    // when:
    mplNotify.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand("/execute @e[name=" + mplNotify.getProcess() + NOTIFY + "] ~ ~ ~ "
            + getOnCommand("~ ~ ~")), //
        new InternalCommand("/kill @e[name=" + mplNotify.getProcess() + NOTIFY + "]"));
  }

  @Test
  public void test_conditional_Notify() {
    // given:
    MplNotify mplNotify = some($MplNotify()//
        .withConditional(CONDITIONAL));

    // when:
    mplNotify.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand("/execute @e[name=" + mplNotify.getProcess() + NOTIFY + "] ~ ~ ~ "
            + getOnCommand("~ ~ ~"), true), //
        new InternalCommand("/kill @e[name=" + mplNotify.getProcess() + NOTIFY + "]", true));
  }

  @Test
  public void test_invert_Notify() {
    // given:
    Mode mode = some($Enum(Mode.class));
    MplNotify mplNotify = some($MplNotify()//
        .withConditional(INVERT)//
        .withPrevious(new Dependable() {
          @Override
          public boolean canBeDependedOn() {
            return true;
          }

          @Override
          public Mode getModeForInverting() {
            return mode;
          }
        }));

    // when:
    mplNotify.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InvertingCommand(mode), //
        new InternalCommand("/execute @e[name=" + mplNotify.getProcess() + NOTIFY + "] ~ ~ ~ "
            + getOnCommand("~ ~ ~"), true), //
        new InternalCommand("/kill @e[name=" + mplNotify.getProcess() + NOTIFY + "]", true));
  }

  // @formatter:off
  // ----------------------------------------------------------------------------------------------------
  //    ____                     _                   _         _
  //   | __ )  _ __  ___   __ _ | | __ _ __    ___  (_) _ __  | |_
  //   |  _ \ | '__|/ _ \ / _` || |/ /| '_ \  / _ \ | || '_ \ | __|
  //   | |_) || |  |  __/| (_| ||   < | |_) || (_) || || | | || |_
  //   |____/ |_|   \___| \__,_||_|\_\| .__/  \___/ |_||_| |_| \__|
  //                                  |_|
  // ----------------------------------------------------------------------------------------------------
  // @formatter:on

  @Test
  public void test_when_visiting_a_breakpoint_the_breakpoint_process_is_added() {
    // given:
    MplBreakpoint mplBreakpoint = some($MplBreakpoint()//
        .withConditional(UNCONDITIONAL));

    MplProgram program = some($MplProgram());
    program.addProcess(some($MplProcess()//
        .withChainParts(listOf(mplBreakpoint))));

    // when:
    program.accept(underTest);

    // then:
    Condition<CommandChain> condition = new Condition<CommandChain>() {
      @Override
      public boolean matches(CommandChain value) {
        return "breakpoint".equals(value.getName());
      }
    };
    assertThat(underTest.chains).haveExactly(1, condition);
  }

  // @formatter:off
  // ----------------------------------------------------------------------------------------------------
  //    ___   __             _____  _                              _____  _
  //   |_ _| / _|           |_   _|| |__    ___  _ __             | ____|| | ___   ___
  //    | | | |_              | |  | '_ \  / _ \| '_ \            |  _|  | |/ __| / _ \
  //    | | |  _|  _  _  _    | |  | | | ||  __/| | | |  _  _  _  | |___ | |\__ \|  __/
  //   |___||_|   (_)(_)(_)   |_|  |_| |_| \___||_| |_| (_)(_)(_) |_____||_||___/ \___|
  //
  // ----------------------------------------------------------------------------------------------------
  // @formatter:on

  @Test
  public void test_If_modifier_gelten_fuer_condition() {
    // given:
    MplIf mplIf = some($MplIf()//
        .withMode($Enum(Mode.class))//
        .withConditional($oneOf(UNCONDITIONAL, CONDITIONAL))//
        .withNeedsRedstone($boolean()));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition(), mplIf.getMode(), mplIf.isConditional(),
            mplIf.getNeedsRedstone()) //
    );
  }

  @Test
  public void test_If_modifier_mit_invert_gelten_fuer_condition() {
    // given:
    Mode mode = some($Enum(Mode.class));

    MplIf mplIf = some($MplIf()//
        .withMode($Enum(Mode.class))//
        .withConditional(INVERT)//
        .withNeedsRedstone($boolean())//
        .withPrevious(new Dependable() {
          @Override
          public boolean canBeDependedOn() {
            return true;
          }

          @Override
          public Mode getModeForInverting() {
            return mode;
          }
        }));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InvertingCommand(mode),
        new InternalCommand(mplIf.getCondition(), mplIf.getMode(), true, mplIf.getNeedsRedstone()) //
    );
  }

  @Test
  public void test_If_then_mit_skip_wirft_exception() {
    // given:
    MplIf mplIf = some($MplIf()//
        .withNot(false)//
        .withThenParts(listOf(some($MplSkip()))));

    // when:
    Exception act = null;
    try {
      mplIf.accept(underTest);
    } catch (IllegalStateException ex) {
      act = ex;
    }

    // then:
    assertThat(act).isNotNull();
    assertThat(act.getMessage()).isEqualTo("If cannot contain skip");
  }

  @Test
  public void test_If_else_mit_skip_wirft_exception() {
    // given:
    MplIf mplIf = some($MplIf()//
        .withNot(false)//
        .withElseParts(listOf(some($MplSkip()))));

    // when:
    Exception act = null;
    try {
      mplIf.accept(underTest);
    } catch (IllegalStateException ex) {
      act = ex;
    }

    // then:
    assertThat(act).isNotNull();
    assertThat(act.getMessage()).isEqualTo("If cannot contain skip");
  }

  @Test
  public void test_If_mit_nur_einem_then_wird_zu_einem_conditional() {
    // given:
    MplCommand then1 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplIf mplIf = some($MplIf()//
        .withNot(false)//
        .withThenParts(listOf(then1)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new InternalCommand(then1.getCommand(), then1.getMode(), true, then1.getNeedsRedstone())//
    );
  }

  @Test
  public void test_If_mit_nur_einem_else_wird_zu_einem_invert() {
    // given:
    MplCommand else1 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplIf mplIf = some($MplIf()//
        .withNot(false)//
        .withElseParts(listOf(else1)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new InvertingCommand(CHAIN), //
        new InternalCommand(else1.getCommand(), else1.getMode(), true, else1.getNeedsRedstone())//
    );
  }

  @Test
  public void test_If_not_mit_nur_einem_then_wird_zu_einem_invert() {
    // given:
    MplCommand then1 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplIf mplIf = some($MplIf()//
        .withNot(true)//
        .withThenParts(listOf(then1)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new InvertingCommand(CHAIN), //
        new InternalCommand(then1.getCommand(), then1.getMode(), true, then1.getNeedsRedstone())//
    );
  }

  @Test
  public void test_If_not_mit_nur_einem_else_wird_zu_einem_conditional() {
    // given:
    MplCommand else1 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplIf mplIf = some($MplIf()//
        .withNot(true)//
        .withElseParts(listOf(else1)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new InternalCommand(else1.getCommand(), else1.getMode(), true, else1.getNeedsRedstone())//
    );
  }

  @Test
  public void test_If_mit_then_und_else_wird_zu_conditional_und_invert() {
    // given:
    MplCommand then1 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand else1 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplIf mplIf = some($MplIf()//
        .withNot(false)//
        .withThenParts(listOf(then1))//
        .withElseParts(listOf(else1)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new InternalCommand(then1.getCommand(), then1.getMode(), true, then1.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 2} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(else1.getCommand(), else1.getMode(), true, else1.getNeedsRedstone())//
    );
  }

  @Test
  public void test_If_mit_meheren_then___erster_conditional_andere_SuccessCount_1() {
    // given:
    MplCommand then1 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand then2 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand then3 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplIf mplIf = some($MplIf()//
        .withNot(false)//
        .withThenParts(listOf(then1, then2, then3)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new NormalizingCommand(), //
        new InternalCommand(then1.getCommand(), then1.getMode(), true, then1.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 2} chain_command_block -1 {SuccessCount:1}"), //
        new InternalCommand(then2.getCommand(), then2.getMode(), true, then2.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 4} chain_command_block -1 {SuccessCount:1}"), //
        new InternalCommand(then3.getCommand(), then3.getMode(), true, then3.getNeedsRedstone())//
    );
  }

  @Test
  public void test_If_not_mit_meheren_then___alle_SuccessCount_0() {
    // given:
    MplCommand then1 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand then2 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand then3 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplIf mplIf = some($MplIf()//
        .withNot(true)//
        .withThenParts(listOf(then1, then2, then3)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new InternalCommand("/testforblock ${this - 1} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(then1.getCommand(), then1.getMode(), true, then1.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 3} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(then2.getCommand(), then2.getMode(), true, then2.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 5} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(then3.getCommand(), then3.getMode(), true, then3.getNeedsRedstone())//
    );
  }

  @Test
  public void test_If_mit_meheren_else___alle_SuccessCount_0() {
    // given:
    MplCommand else1 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand else2 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand else3 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplIf mplIf = some($MplIf()//
        .withNot(false)//
        .withElseParts(listOf(else1, else2, else3)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new InternalCommand("/testforblock ${this - 1} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(else1.getCommand(), else1.getMode(), true, else1.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 3} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(else2.getCommand(), else2.getMode(), true, else2.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 5} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(else3.getCommand(), else3.getMode(), true, else3.getNeedsRedstone())//
    );
  }

  @Test
  public void test_If_not_mit_meheren_else___erster_conditional_andere_SuccessCount_1() {
    // given:
    MplCommand else1 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand else2 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand else3 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplIf mplIf = some($MplIf()//
        .withNot(true)//
        .withElseParts(listOf(else1, else2, else3)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new NormalizingCommand(), //
        new InternalCommand(else1.getCommand(), else1.getMode(), true, else1.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 2} chain_command_block -1 {SuccessCount:1}"), //
        new InternalCommand(else2.getCommand(), else2.getMode(), true, else2.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 4} chain_command_block -1 {SuccessCount:1}"), //
        new InternalCommand(else3.getCommand(), else3.getMode(), true, else3.getNeedsRedstone())//
    );
  }

  @Test
  public void test_If_mit_meheren_then_und_else() {
    // given:
    MplCommand then1 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand then2 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand then3 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand else1 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand else2 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand else3 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplIf mplIf = some($MplIf()//
        .withNot(false)//
        .withThenParts(listOf(then1, then2, then3))//
        .withElseParts(listOf(else1, else2, else3)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new NormalizingCommand(), //
        // then
        new InternalCommand(then1.getCommand(), then1.getMode(), true, then1.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 2} chain_command_block -1 {SuccessCount:1}"), //
        new InternalCommand(then2.getCommand(), then2.getMode(), true, then2.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 4} chain_command_block -1 {SuccessCount:1}"), //
        new InternalCommand(then3.getCommand(), then3.getMode(), true, then3.getNeedsRedstone()), //
        // else
        new InternalCommand("/testforblock ${this - 6} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(else1.getCommand(), else1.getMode(), true, else1.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 8} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(else2.getCommand(), else2.getMode(), true, else2.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 10} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(else3.getCommand(), else3.getMode(), true, else3.getNeedsRedstone())//
    );
  }

  @Test
  public void test_If_not_mit_meheren_then_und_else() {
    // given:
    MplCommand then1 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand then2 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand then3 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand else1 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand else2 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand else3 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplIf mplIf = some($MplIf()//
        .withNot(true)//
        .withThenParts(listOf(then1, then2, then3))//
        .withElseParts(listOf(else1, else2, else3)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new NormalizingCommand(), //
        // then
        new InternalCommand("/testforblock ${this - 1} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(then1.getCommand(), then1.getMode(), true, then1.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 3} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(then2.getCommand(), then2.getMode(), true, then2.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 5} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(then3.getCommand(), then3.getMode(), true, then3.getNeedsRedstone()), //
        // else
        new InternalCommand("/testforblock ${this - 7} chain_command_block -1 {SuccessCount:1}"), //
        new InternalCommand(else1.getCommand(), else1.getMode(), true, else1.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 9} chain_command_block -1 {SuccessCount:1}"), //
        new InternalCommand(else2.getCommand(), else2.getMode(), true, else2.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 11} chain_command_block -1 {SuccessCount:1}"), //
        new InternalCommand(else3.getCommand(), else3.getMode(), true, else3.getNeedsRedstone())//
    );
  }

  @Test
  public void test_If_mit_conditional_im_then() {
    // given:
    MplCommand then1 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand then2 = some($MplCommand().withConditional(CONDITIONAL));
    MplCommand then3 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplIf mplIf = some($MplIf()//
        .withNot(false)//
        .withThenParts(listOf(then1, then2, then3)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new NormalizingCommand(), //
        new InternalCommand(then1.getCommand(), then1.getMode(), true, then1.getNeedsRedstone()), //
        new InternalCommand(then2.getCommand(), then2.getMode(), true, then2.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 3} chain_command_block -1 {SuccessCount:1}"), //
        new InternalCommand(then3.getCommand(), then3.getMode(), true, then3.getNeedsRedstone())//
    );
  }

  @Test
  public void test_If_mit_conditional_im_then___kein_normalizer() {
    // given:
    MplCommand then1 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand then2 = some($MplCommand().withConditional(CONDITIONAL));
    MplIf mplIf = some($MplIf()//
        .withNot(false)//
        .withThenParts(listOf(then1, then2)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new InternalCommand(then1.getCommand(), then1.getMode(), true, then1.getNeedsRedstone()), //
        new InternalCommand(then2.getCommand(), then2.getMode(), true, then2.getNeedsRedstone())//
    );
  }

  @Test
  public void test_If_mit_invert_im_then() {
    // given:
    MplCommand then1 = some($MplCommand()//
        .withConditional(UNCONDITIONAL));

    MplCommand then2 = some($MplCommand()//
        .withConditional(INVERT)//
        .withPrevious(then1));

    MplCommand then3 = some($MplCommand()//
        .withConditional(UNCONDITIONAL));

    MplIf mplIf = some($MplIf()//
        .withNot(false)//
        .withThenParts(listOf(then1, then2, then3)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new NormalizingCommand(), //
        new InternalCommand(then1.getCommand(), then1.getMode(), true, then1.getNeedsRedstone()), //
        new InvertingCommand(then1.getMode()), //
        new InternalCommand("/testforblock ${this - 3} chain_command_block -1 {SuccessCount:1}",
            true), //
        new InternalCommand(then2.getCommand(), then2.getMode(), true, then2.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 5} chain_command_block -1 {SuccessCount:1}"), //
        new InternalCommand(then3.getCommand(), then3.getMode(), true, then3.getNeedsRedstone())//
    );
  }

  @Test
  public void test_If_mit_conditional_im_else() {
    // given:
    MplCommand else1 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand else2 = some($MplCommand().withConditional(CONDITIONAL));
    MplCommand else3 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplIf mplIf = some($MplIf()//
        .withNot(false)//
        .withElseParts(listOf(else1, else2, else3)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new InternalCommand("/testforblock ${this - 1} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(else1.getCommand(), else1.getMode(), true, else1.getNeedsRedstone()), //
        new InternalCommand(else2.getCommand(), else2.getMode(), true, else2.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 4} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(else3.getCommand(), else3.getMode(), true, else3.getNeedsRedstone())//
    );
  }

  @Test
  public void test_If_mit_invert_im_else() {
    // given:
    MplCommand else1 = some($MplCommand()//
        .withConditional(UNCONDITIONAL));

    MplCommand else2 = some($MplCommand()//
        .withConditional(INVERT)//
        .withPrevious(else1));

    MplCommand else3 = some($MplCommand()//
        .withConditional(UNCONDITIONAL));

    MplIf mplIf = some($MplIf()//
        .withNot(false)//
        .withElseParts(listOf(else1, else2, else3)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new InternalCommand("/testforblock ${this - 1} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(else1.getCommand(), else1.getMode(), true, else1.getNeedsRedstone()), //
        new InvertingCommand(else1.getMode()), //
        new InternalCommand("/testforblock ${this - 4} chain_command_block -1 {SuccessCount:0}",
            true), //
        new InternalCommand(else2.getCommand(), else2.getMode(), true, else2.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 6} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(else3.getCommand(), else3.getMode(), true, else3.getNeedsRedstone())//
    );
  }

  @Test
  public void test_If_not_mit_conditional_im_then() {
    // given:
    MplCommand then1 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand then2 = some($MplCommand().withConditional(CONDITIONAL));
    MplCommand then3 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplIf mplIf = some($MplIf()//
        .withNot(true)//
        .withThenParts(listOf(then1, then2, then3)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new InternalCommand("/testforblock ${this - 1} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(then1.getCommand(), then1.getMode(), true, then1.getNeedsRedstone()), //
        new InternalCommand(then2.getCommand(), then2.getMode(), true, then2.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 4} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(then3.getCommand(), then3.getMode(), true, then3.getNeedsRedstone())//
    );
  }

  @Test
  public void test_If_not_mit_invert_im_then() {
    // given:
    MplCommand then1 = some($MplCommand()//
        .withConditional(UNCONDITIONAL));

    MplCommand then2 = some($MplCommand()//
        .withConditional(INVERT)//
        .withPrevious(then1));

    MplCommand then3 = some($MplCommand()//
        .withConditional(UNCONDITIONAL));
    MplIf mplIf = some($MplIf()//
        .withNot(true)//
        .withThenParts(listOf(then1, then2, then3)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new InternalCommand("/testforblock ${this - 1} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(then1.getCommand(), then1.getMode(), true, then1.getNeedsRedstone()), //
        new InvertingCommand(then1.getMode()), //
        new InternalCommand("/testforblock ${this - 4} chain_command_block -1 {SuccessCount:0}",
            true), //
        new InternalCommand(then2.getCommand(), then2.getMode(), true, then2.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 6} chain_command_block -1 {SuccessCount:0}"), //
        new InternalCommand(then3.getCommand(), then3.getMode(), true, then3.getNeedsRedstone())//
    );
  }

  @Test
  public void test_If_not_mit_conditional_im_else() {
    // given:
    MplCommand else1 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand else2 = some($MplCommand().withConditional(CONDITIONAL));
    MplCommand else3 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplIf mplIf = some($MplIf()//
        .withNot(true)//
        .withElseParts(listOf(else1, else2, else3)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new NormalizingCommand(), //
        new InternalCommand(else1.getCommand(), else1.getMode(), true, else1.getNeedsRedstone()), //
        new InternalCommand(else2.getCommand(), else2.getMode(), true, else2.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 3} chain_command_block -1 {SuccessCount:1}"), //
        new InternalCommand(else3.getCommand(), else3.getMode(), true, else3.getNeedsRedstone())//
    );
  }

  @Test
  public void test_If_not_mit_conditional_im_else___kein_normalizer() {
    // given:
    MplCommand else1 = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand else2 = some($MplCommand().withConditional(CONDITIONAL));
    MplIf mplIf = some($MplIf()//
        .withNot(true)//
        .withElseParts(listOf(else1, else2)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new InternalCommand(else1.getCommand(), else1.getMode(), true, else1.getNeedsRedstone()), //
        new InternalCommand(else2.getCommand(), else2.getMode(), true, else2.getNeedsRedstone())//
    );
  }

  @Test
  public void test_If_not_mit_invert_im_else() {
    // given:
    MplCommand else1 = some($MplCommand()//
        .withConditional(UNCONDITIONAL));

    MplCommand else2 = some($MplCommand()//
        .withConditional(INVERT)//
        .withPrevious(else1));

    MplCommand else3 = some($MplCommand()//
        .withConditional(UNCONDITIONAL));

    MplIf mplIf = some($MplIf()//
        .withNot(true)//
        .withElseParts(listOf(else1, else2, else3)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new NormalizingCommand(), //
        new InternalCommand(else1.getCommand(), else1.getMode(), true, else1.getNeedsRedstone()), //
        new InvertingCommand(else1.getMode()), //
        new InternalCommand("/testforblock ${this - 3} chain_command_block -1 {SuccessCount:1}",
            true), //
        new InternalCommand(else2.getCommand(), else2.getMode(), true, else2.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 5} chain_command_block -1 {SuccessCount:1}"), //
        new InternalCommand(else3.getCommand(), else3.getMode(), true, else3.getNeedsRedstone())//
    );
  }

  /**
   * <pre>
   * if: /mplIf
   * then (
   *   if: /outer
   *   then (
   *     /innerThen
   *   ) else (
   *     /innerElse
   *   )
   * )
   * </pre>
   */
  @Test
  public void test_If_mit_kleinem_nested_then() {
    // given:

    MplCommand innerThen = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand innerElse = some($MplCommand().withConditional(UNCONDITIONAL));
    MplIf outer = some($MplIf()//
        .withNot(false)//
        .withThenParts(listOf(innerThen))//
        .withElseParts(listOf(innerElse)));

    MplIf mplIf = some($MplIf()//
        .withNot(false)//
        .withThenParts(listOf(outer)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new NormalizingCommand(), //
        new InternalCommand(outer.getCondition(), true), //
        new InternalCommand(innerThen.getCommand(), innerThen.getMode(), true,
            innerThen.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 3} chain_command_block -1 {SuccessCount:1}"), //
        new InternalCommand("/testforblock ${this - 3} chain_command_block -1 {SuccessCount:0}",
            true), //
        new InternalCommand(innerElse.getCommand(), innerElse.getMode(), true,
            innerElse.getNeedsRedstone())//
    );
  }

  /**
   * <pre>
   * if: /mplIf
   * then (
   *   /outerThen1
   *   if: /outerThen2
   *   then (
   *     /innerThen1
   *   ) else (
   *     /innerElse1
   *   )
   *   /outerThen3
   * )
   * </pre>
   */
  @Test
  public void test_If_mit_nested_then() {
    // given:
    MplCommand outer1 = some($MplCommand().withConditional(UNCONDITIONAL));

    MplCommand innerThen = some($MplCommand().withConditional(UNCONDITIONAL));
    MplCommand innerElse = some($MplCommand().withConditional(UNCONDITIONAL));
    MplIf outer2 = some($MplIf()//
        .withNot(false)//
        .withThenParts(listOf(innerThen))//
        .withElseParts(listOf(innerElse)));

    MplCommand outer3 = some($MplCommand().withConditional(UNCONDITIONAL));

    MplIf mplIf = some($MplIf()//
        .withNot(false)//
        .withThenParts(listOf(outer1, outer2, outer3)));

    // when:
    mplIf.accept(underTest);

    // then:
    assertThat(underTest.commands).containsExactly(//
        new InternalCommand(mplIf.getCondition()), //
        new NormalizingCommand(), //
        new InternalCommand(outer1.getCommand(), outer1.getMode(), true, outer1.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 2} chain_command_block -1 {SuccessCount:1}"), //
        new InternalCommand(outer2.getCondition(), true), //
        new InternalCommand(innerThen.getCommand(), innerThen.getMode(), true,
            innerThen.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 5} chain_command_block -1 {SuccessCount:1}"), //
        new InternalCommand("/testforblock ${this - 3} chain_command_block -1 {SuccessCount:0}",
            true), //
        new InternalCommand(innerElse.getCommand(), innerElse.getMode(), true,
            innerElse.getNeedsRedstone()), //
        new InternalCommand("/testforblock ${this - 8} chain_command_block -1 {SuccessCount:1}"), //
        new InternalCommand(outer3.getCommand(), outer3.getMode(), true, outer3.getNeedsRedstone())//
    );
  }

  // @formatter:off
  // ----------------------------------------------------------------------------------------------------
  //   __        __ _      _  _
  //   \ \      / /| |__  (_)| |  ___
  //    \ \ /\ / / | '_ \ | || | / _ \
  //     \ V  V /  | | | || || ||  __/
  //      \_/\_/   |_| |_||_||_| \___|
  //
  // ----------------------------------------------------------------------------------------------------
  // @formatter:on

  @Test
  public void test_While_mit_skip_wirft_exception() {
    // given:
    MplWhile mplIf = some($MplWhile()//
        .withChainParts(listOf(some($MplSkip()))));

    // when:
    Exception act = null;
    try {
      mplIf.accept(underTest);
    } catch (IllegalStateException ex) {
      act = ex;
    }

    // then:
    assertThat(act).isNotNull();
    assertThat(act.getMessage()).isEqualTo("while cannot contain skip");
  }

  @Test
  public void test_While_repeat_modifier_gelten_fuer_condition() {
    // given:
    MplWhile mplWhile = some($MplWhile()//
        .withTrailing(false)//
        .withMode($Enum(Mode.class))//
        .withConditional($oneOf(UNCONDITIONAL, CONDITIONAL))//
        .withNeedsRedstone($boolean()));

    // when:
    mplWhile.accept(underTest);

    // then:
    assertThat(underTest.commands).startsWith(//
        new Command(mplWhile.getCondition(), mplWhile)//
    );
  }

  @Test
  public void test_While_repeat_modifier_mit_invert_gelten_fuer_condition() {
    // given:
    Mode mode = some($Enum(Mode.class));

    MplWhile mplWhile = some($MplWhile()//
        .withTrailing(false)//
        .withMode($Enum(Mode.class))//
        .withConditional(INVERT)//
        .withNeedsRedstone($boolean())//
        .withPrevious(new Dependable() {
          @Override
          public boolean canBeDependedOn() {
            return true;
          }

          @Override
          public Mode getModeForInverting() {
            return mode;
          }
        }));

    // when:
    mplWhile.accept(underTest);

    // then:
    assertThat(underTest.commands).startsWith(//
        new InvertingCommand(mode), //
        new Command(mplWhile.getCondition(), mplWhile)//
    );
  }

  @Test
  public void test_repeat_While_unconditional_modifier_gelten_fuer_init_command() {
    // given:
    MplWhile mplWhile = some($MplWhile()//
        .withTrailing(true)//
        .withMode($Enum(Mode.class))//
        .withConditional($oneOf(UNCONDITIONAL))//
        .withNeedsRedstone($boolean()));

    // when:
    mplWhile.accept(underTest);

    // then:
    assertThat(underTest.commands).startsWith(//
        new InternalCommand(getOnCommand("${this + 1}"), mplWhile)//
    );
  }

  @Test
  public void test_repeat_While_conditional_modifier_erzeugt_conditional_jump() {
    // given:
    MplWhile mplWhile = some($MplWhile()//
        .withTrailing(true)//
        .withMode($Enum(Mode.class))//
        .withConditional($oneOf(CONDITIONAL))//
        .withNeedsRedstone($boolean()));

    // when:
    mplWhile.accept(underTest);

    // then:
    int ref = underTest.commands.size() - 3;
    if (underTest.options.hasOption(TRANSMITTER)) {
      ref--;
    }
    assertThat(underTest.commands).startsWith(//
        new InternalCommand(getOnCommand("${this + 3}"), mplWhile), //
        new InvertingCommand(CHAIN), //
        new InternalCommand(getOnCommand("${this + " + ref + "}"), true)//
    );
  }

  @Test
  public void test_repeat_While_invert_modifier_erzeugt_invert_jump() {
    // given:
    Mode mode = some($Enum(Mode.class));

    MplWhile mplWhile = some($MplWhile()//
        .withTrailing(true)//
        .withMode($Enum(Mode.class))//
        .withConditional(INVERT)//
        .withNeedsRedstone($boolean())//
        .withPrevious(new Dependable() {
          @Override
          public boolean canBeDependedOn() {
            return true;
          }

          @Override
          public Mode getModeForInverting() {
            return mode;
          }
        }));

    // when:
    mplWhile.accept(underTest);

    // then:
    int ref = underTest.commands.size() - 1;
    if (underTest.options.hasOption(TRANSMITTER)) {
      ref--;
    }
    assertThat(underTest.commands).startsWith(//
        new InternalCommand(getOnCommand("${this + " + ref + "}"), mplWhile), //
        new InvertingCommand(CHAIN), //
        new InternalCommand(getOnCommand("${this + 1}"), true)//
    );
  }

  @Test
  public void test_repeat_unconditional_modifier_gelten_fuer_init_command() {
    // given:
    MplWhile mplWhile = some($MplWhile()//
        .withCondition((String) null)//
        .withNot($boolean())//
        .withTrailing($boolean())//
        .withMode($Enum(Mode.class))//
        .withConditional($oneOf(UNCONDITIONAL))//
        .withNeedsRedstone($boolean()));

    // when:
    mplWhile.accept(underTest);

    // then:
    assertThat(underTest.commands).startsWith(//
        new InternalCommand(getOnCommand("${this + 1}"), mplWhile)//
    );
  }

  @Test
  public void test_repeat_conditional_modifier_erzeugt_conditional_jump() {
    // given:
    MplWhile mplWhile = some($MplWhile()//
        .withCondition((String) null)//
        .withNot($boolean())//
        .withTrailing($boolean())//
        .withMode($Enum(Mode.class))//
        .withConditional($oneOf(CONDITIONAL))//
        .withNeedsRedstone($boolean()));

    // when:
    mplWhile.accept(underTest);

    // then:
    int ref = underTest.commands.size() - 3;
    if (underTest.options.hasOption(TRANSMITTER)) {
      ref--;
    }
    assertThat(underTest.commands).startsWith(//
        new InternalCommand(getOnCommand("${this + 3}"), mplWhile), //
        new InvertingCommand(CHAIN), //
        new InternalCommand(getOnCommand("${this + " + ref + "}"), true)//
    );
  }

  @Test
  public void test_repeat_invert_modifier_erzeugt_invert_jump() {
    // given:
    Mode mode = some($Enum(Mode.class));

    MplWhile mplWhile = some($MplWhile()//
        .withCondition((String) null)//
        .withNot($boolean())//
        .withTrailing($boolean())//
        .withMode($Enum(Mode.class))//
        .withConditional(INVERT)//
        .withNeedsRedstone($boolean())//
        .withPrevious(new Dependable() {
          @Override
          public boolean canBeDependedOn() {
            return true;
          }

          @Override
          public Mode getModeForInverting() {
            return mode;
          }
        }));

    // when:
    mplWhile.accept(underTest);

    // then:
    int ref = underTest.commands.size() - 1;
    if (underTest.options.hasOption(TRANSMITTER)) {
      ref--;
    }
    assertThat(underTest.commands).startsWith(//
        new InternalCommand(getOnCommand("${this + " + ref + "}"), mplWhile), //
        new InvertingCommand(CHAIN), //
        new InternalCommand(getOnCommand("${this + 1}"), true)//
    );
  }

  @Test
  public void test_While_mit_Waitfor_hat_korrekte_Referenzen_zum_Ende() {
    // given:
    MplWhile mplWhile = some($MplWhile()//
        .withNot(false)//
        .withTrailing(false)//
        .withConditional(UNCONDITIONAL)//
        .withChainParts(listOf(some($MplWaitfor()))));

    // when:
    mplWhile.accept(underTest);

    // then:
    int ref = underTest.commands.size() - 1;
    if (underTest.options.hasOption(TRANSMITTER)) {
      ref--;
    }
    int jumpIndex = 3;
    ReferencingCommand jump = (ReferencingCommand) underTest.commands.get(jumpIndex);
    assertThat(jumpIndex + jump.getRelative()).isEqualTo(ref);
  }

  // @formatter:off
  // ----------------------------------------------------------------------------------------------------
  //    ____                     _
  //   | __ )  _ __  ___   __ _ | | __
  //   |  _ \ | '__|/ _ \ / _` || |/ /
  //   | |_) || |  |  __/| (_| ||   <
  //   |____/ |_|   \___| \__,_||_|\_\
  //
  // ----------------------------------------------------------------------------------------------------
  // @formatter:on

  private int findFirstReciever(List<ChainLink> chainLinks) {
    for (int i = 0; i < chainLinks.size(); i++) {
      ChainLink chainLink = chainLinks.get(i);
      if (isReciever(chainLink)) {
        return i;
      }
    }
    return -1;
  }

  private int findSecondReciever(List<ChainLink> chainLinks) {
    int startIndex = findFirstReciever(chainLinks) + 1;
    for (int i = startIndex; i < chainLinks.size(); i++) {
      ChainLink chainLink = chainLinks.get(i);
      if (isReciever(chainLink)) {
        return i;
      }
    }
    return -1;
  }

  private int findLastReciever(List<ChainLink> chainLinks) {
    for (int i = chainLinks.size() - 1; i >= 0; i--) {
      ChainLink chainLink = chainLinks.get(i);
      if (isReciever(chainLink)) {
        return i;
      }
    }
    return -1;
  }

  private boolean isReciever(ChainLink chainLink) {
    if (underTest.options.hasOption(TRANSMITTER)) {
      if (chainLink instanceof MplSkip) {
        return true;
      }
    } else {
      if (chainLink instanceof Command && ((Command) chainLink).getMode() == IMPULSE) {
        return true;
      }
    }
    return false;
  }

  @Test
  public void test_unconditional_Break() {
    // given:
    MplCommand command = some($MplCommand().withConditional(UNCONDITIONAL));
    MplWhile mplWhile = some($MplWhile());
    MplBreak mplBreak = some($MplBreak()//
        .withLoop(mplWhile)//
        .withConditional(UNCONDITIONAL));
    mplWhile.setChainParts(listOf(command, mplBreak));

    // when:
    mplWhile.accept(underTest);

    // then:
    List<ChainLink> commands = underTest.commands;
    int entry = findFirstReciever(commands);
    int exit = findLastReciever(commands);
    int beforeBreak = commands.indexOf(new Command(command.getCommand(), command));
    commands = commands.subList(beforeBreak + 1, commands.size());

    assertThat(commands).startsWith(//
        new ReferencingCommand(getOnCommand(REF), mplBreak.getMode(), false,
            mplBreak.getNeedsRedstone(), exit - (beforeBreak + 1)), //
        new ReferencingCommand(getOffCommand(REF), true, entry - (beforeBreak + 2))//
    );
    assertThat(commands.size()).isBetween(3, 4);
  }

  @Test
  public void test_conditional_Break() {
    // given:
    MplCommand command = some($MplCommand().withConditional(UNCONDITIONAL));
    MplWhile mplWhile = some($MplWhile());
    MplBreak mplBreak = some($MplBreak()//
        .withLoop(mplWhile)//
        .withConditional(CONDITIONAL));
    mplWhile.setChainParts(listOf(command, mplBreak));

    // when:
    mplWhile.accept(underTest);

    // then:
    List<ChainLink> commands = underTest.commands;
    int entry = findFirstReciever(commands);
    int exit = findLastReciever(commands);
    int beforeBreak = commands.indexOf(new Command(command.getCommand(), command));
    commands = commands.subList(beforeBreak + 1, commands.size());

    assertThat(commands).startsWith(//
        new ReferencingCommand(getOnCommand(REF), mplBreak.getMode(), true,
            mplBreak.getNeedsRedstone(), exit - (beforeBreak + 1)), //
        new ReferencingCommand(getOffCommand(REF), true, entry - (beforeBreak + 2)), //
        new InvertingCommand(CHAIN), //
        new ReferencingCommand(getOnCommand(REF), true, 1)//
    );
  }

  @Test
  public void test_invert_Break() {
    // given:
    MplCommand command = some($MplCommand().withConditional(UNCONDITIONAL));
    MplWhile mplWhile = some($MplWhile());
    MplBreak mplBreak = some($MplBreak()//
        .withLoop(mplWhile)//
        .withConditional(INVERT)//
        .withPrevious(command));
    mplWhile.setChainParts(listOf(command, mplBreak));

    // when:
    mplWhile.accept(underTest);

    // then:
    List<ChainLink> commands = underTest.commands;
    int entry = findFirstReciever(commands);
    int exit = findLastReciever(commands);
    int beforeBreak = commands.indexOf(new Command(command.getCommand(), command));
    commands = commands.subList(beforeBreak + 1, commands.size());

    assertThat(commands).startsWith(//
        new ReferencingCommand(getOnCommand(REF), mplBreak.getMode(), true,
            mplBreak.getNeedsRedstone(), 4), //
        new InvertingCommand(CHAIN), //
        new ReferencingCommand(getOnCommand(REF), true, exit - (beforeBreak + 3)), //
        new ReferencingCommand(getOffCommand(REF), true, entry - (beforeBreak + 4))//
    );
  }

  @Test
  public void test_nested_Break_stops_all_inner_loops() {
    // given:
    MplCommand command = some($MplCommand()//
        .withCommand("command")//
        .withConditional(UNCONDITIONAL));
    MplWhile innerWhile = some($MplWhile()//
        .withCondition((String) null));

    MplWhile outerWhile = some($MplWhile()//
        .withCondition((String) null)//
        .withChainParts(listOf(innerWhile)));

    MplBreak mplBreak = some($MplBreak()//
        .withLoop(outerWhile)//
        .withConditional(UNCONDITIONAL)//
        .withPrevious(command));
    innerWhile.setChainParts(listOf(command, mplBreak));

    // when:
    outerWhile.accept(underTest);

    // then:
    List<ChainLink> commands = underTest.commands;
    int outerEntry = findFirstReciever(commands);
    int innerEntry = findSecondReciever(commands);
    int outerExit = findLastReciever(commands);
    int beforeBreak = commands.indexOf(new Command(command.getCommand(), command));
    commands = commands.subList(beforeBreak + 1, commands.size());

    assertThat(commands).startsWith(//
        new ReferencingCommand(getOnCommand(REF), mplBreak.getMode(), false,
            mplBreak.getNeedsRedstone(), outerExit - (beforeBreak + 1)), //
        new ReferencingCommand(getOffCommand(REF), true, innerEntry - (beforeBreak + 2)), //
        new ReferencingCommand(getOffCommand(REF), true, outerEntry - (beforeBreak + 3))//
    );
  }

  // @formatter:off
  // ----------------------------------------------------------------------------------------------------
  //     ____               _    _
  //    / ___| ___   _ __  | |_ (_) _ __   _   _   ___
  //   | |    / _ \ | '_ \ | __|| || '_ \ | | | | / _ \
  //   | |___| (_) || | | || |_ | || | | || |_| ||  __/
  //    \____|\___/ |_| |_| \__||_||_| |_| \__,_| \___|
  //
  // ----------------------------------------------------------------------------------------------------
  // @formatter:on

  @Test
  public void test_unconditional_Continue_without_condition() {
    // given:
    MplCommand command = some($MplCommand().withConditional(UNCONDITIONAL));
    MplWhile mplWhile = some($MplWhile()//
        .withCondition((String) null));
    MplContinue mplContinue = some($MplContinue()//
        .withLoop(mplWhile)//
        .withConditional(UNCONDITIONAL));
    mplWhile.setChainParts(listOf(command, mplContinue));

    // when:
    mplWhile.accept(underTest);

    // then:
    List<ChainLink> commands = underTest.commands;
    int entry = findFirstReciever(commands);
    int beforeContinue = commands.indexOf(new Command(command.getCommand(), command));
    commands = commands.subList(beforeContinue + 1, commands.size());

    assertThat(commands).startsWith(//
        new ReferencingCommand(getOffCommand(REF), mplContinue.getMode(), false,
            mplContinue.getNeedsRedstone(), entry - (beforeContinue + 1)), //
        new ReferencingCommand(getOnCommand(REF), true, entry - (beforeContinue + 2))//
    );
    assertThat(commands.size()).isBetween(3, 4);
  }

  @Test
  public void test_unconditional_Continue_with_condition() {
    // given:
    MplCommand command = some($MplCommand().withConditional(UNCONDITIONAL));
    MplWhile mplWhile = some($MplWhile());
    MplContinue mplContinue = some($MplContinue()//
        .withLoop(mplWhile)//
        .withConditional(UNCONDITIONAL));
    mplWhile.setChainParts(listOf(command, mplContinue));

    // when:
    mplWhile.accept(underTest);

    // then:
    List<ChainLink> commands = underTest.commands;
    int entry = findFirstReciever(commands);
    int exit = findLastReciever(commands);
    int beforeContinue = commands.indexOf(new Command(command.getCommand(), command));
    commands = commands.subList(beforeContinue + 1, commands.size());

    assertThat(commands).startsWith(//
        new Command(mplWhile.getCondition(), mplContinue), //
        new ReferencingCommand(getOffCommand(REF), true, entry - (beforeContinue + 2)), //
        new ReferencingCommand(getOnCommand(REF), true, entry - (beforeContinue + 3)), //
        new InvertingCommand(CHAIN), //
        new ReferencingCommand(getOnCommand(REF), true, exit - (beforeContinue + 5)), //
        new ReferencingCommand(getOffCommand(REF), true, entry - (beforeContinue + 6))//
    );
    assertThat(commands.size()).isBetween(7, 8);
  }

  @Test
  public void test_conditional_Continue_without_condition() {
    // given:
    MplCommand command = some($MplCommand().withConditional(UNCONDITIONAL));
    MplWhile mplWhile = some($MplWhile()//
        .withCondition((String) null));
    MplContinue mplContinue = some($MplContinue()//
        .withLoop(mplWhile)//
        .withConditional(CONDITIONAL));
    mplWhile.setChainParts(listOf(command, mplContinue));

    // when:
    mplWhile.accept(underTest);

    // then:
    List<ChainLink> commands = underTest.commands;
    int entry = findFirstReciever(commands);
    int doNothing = findSecondReciever(commands);
    int beforeContinue = commands.indexOf(new Command(command.getCommand(), command));
    commands = commands.subList(beforeContinue + 1, commands.size());

    assertThat(commands).startsWith(//
        new ReferencingCommand(getOffCommand(REF), true, entry - (beforeContinue + 1)), //
        new ReferencingCommand(getOnCommand(REF), true, entry - (beforeContinue + 2)), //
        new ReferencingTestforSuccessCommand(-3, IMPULSE, false), //
        new ReferencingCommand(getOnCommand(REF), true, doNothing - (beforeContinue + 4))//
    );
  }

  @Test
  public void test_conditional_Continue_with_condition() {
    // given:
    MplCommand command = some($MplCommand().withConditional(UNCONDITIONAL));
    MplWhile mplWhile = some($MplWhile());
    MplContinue mplContinue = some($MplContinue()//
        .withLoop(mplWhile)//
        .withConditional(CONDITIONAL));
    mplWhile.setChainParts(listOf(command, mplContinue));

    // when:
    mplWhile.accept(underTest);

    // then:
    List<ChainLink> commands = underTest.commands;
    int entry = findFirstReciever(commands);
    int exit = findLastReciever(commands);
    int doNothing = findSecondReciever(commands);
    int beforeContinue = commands.indexOf(new Command(command.getCommand(), command));
    commands = commands.subList(beforeContinue + 1, commands.size());

    assertThat(commands).startsWith(//
        new NormalizingCommand(), //
        new Command(mplWhile.getCondition(), true), //
        new ReferencingCommand(getOffCommand(REF), true, entry - (beforeContinue + 3)), //
        new ReferencingCommand(getOnCommand(REF), true, entry - (beforeContinue + 4)), //
        new ReferencingTestforSuccessCommand(-4, CHAIN, true), //
        new ReferencingTestforSuccessCommand(-4, CHAIN, false, true), //
        new ReferencingCommand(getOnCommand(REF), true, exit - (beforeContinue + 7)), //
        new ReferencingCommand(getOffCommand(REF), true, entry - (beforeContinue + 8)), //
        new ReferencingTestforSuccessCommand(-8, CHAIN, false), //
        new ReferencingCommand(getOnCommand(REF), true, doNothing - (beforeContinue + 10))//
    );
  }

  @Test
  public void test_invert_Continue_without_condition() {
    // given:
    MplCommand command = some($MplCommand().withConditional(UNCONDITIONAL));
    MplWhile mplWhile = some($MplWhile()//
        .withCondition((String) null));
    MplContinue mplContinue = some($MplContinue()//
        .withLoop(mplWhile)//
        .withConditional(INVERT)//
        .withPrevious(command));
    mplWhile.setChainParts(listOf(command, mplContinue));

    // when:
    mplWhile.accept(underTest);

    // then:
    List<ChainLink> commands = underTest.commands;
    int entry = findFirstReciever(commands);
    int doNothing = findSecondReciever(commands);
    int beforeContinue = commands.indexOf(new Command(command.getCommand(), command));
    commands = commands.subList(beforeContinue + 1, commands.size());

    assertThat(commands).startsWith(//
        new ReferencingCommand(getOnCommand(REF), true, doNothing - (beforeContinue + 1)), //
        new ReferencingTestforSuccessCommand(-2, IMPULSE, false), //
        new ReferencingCommand(getOffCommand(REF), true, entry - (beforeContinue + 3)), //
        new ReferencingCommand(getOnCommand(REF), true, entry - (beforeContinue + 4))//
    );
  }

  @Test
  public void test_invert_Continue_with_condition() {
    // given:
    MplCommand command = some($MplCommand().withConditional(UNCONDITIONAL));
    MplWhile mplWhile = some($MplWhile());
    MplContinue mplContinue = some($MplContinue()//
        .withLoop(mplWhile)//
        .withConditional(INVERT)//
        .withPrevious(command));
    mplWhile.setChainParts(listOf(command, mplContinue));

    // when:
    mplWhile.accept(underTest);

    // then:
    List<ChainLink> commands = underTest.commands;
    int entry = findFirstReciever(commands);
    int exit = findLastReciever(commands);
    int doNothing = findSecondReciever(commands);
    int beforeContinue = commands.indexOf(new Command(command.getCommand(), command));
    commands = commands.subList(beforeContinue + 1, commands.size());

    assertThat(commands).startsWith(//
        new ReferencingCommand(getOnCommand(REF), true, doNothing - (beforeContinue + 1)), //
        new ReferencingTestforSuccessCommand(-2, IMPULSE, false), //
        new Command(mplWhile.getCondition(), true), //
        new ReferencingCommand(getOffCommand(REF), true, entry - (beforeContinue + 4)), //
        new ReferencingCommand(getOnCommand(REF), true, entry - (beforeContinue + 5)), //
        new ReferencingTestforSuccessCommand(-6, IMPULSE, false), //
        new ReferencingTestforSuccessCommand(-4, CHAIN, false, true), //
        new ReferencingCommand(getOnCommand(REF), true, exit - (beforeContinue + 8)), //
        new ReferencingCommand(getOffCommand(REF), true, entry - (beforeContinue + 9))//
    );
  }

  @Test
  public void test_nested_Continue_stops_all_inner_loops() {
    // given:
    MplCommand command = some($MplCommand()//
        .withCommand("command")//
        .withConditional(UNCONDITIONAL));
    MplWhile innerWhile = some($MplWhile()//
        .withCondition((String) null));

    MplWhile outerWhile = some($MplWhile()//
        .withCondition((String) null)//
        .withChainParts(listOf(innerWhile)));

    MplContinue mplContinue = some($MplContinue()//
        .withLoop(outerWhile)//
        .withConditional(UNCONDITIONAL)//
        .withPrevious(command));
    innerWhile.setChainParts(listOf(command, mplContinue));

    // when:
    outerWhile.accept(underTest);

    // then:
    List<ChainLink> commands = underTest.commands;
    int outerEntry = findFirstReciever(commands);
    int innerEntry = findSecondReciever(commands);
    int beforeContinue = commands.indexOf(new Command(command.getCommand(), command));
    commands = commands.subList(beforeContinue + 1, commands.size());

    assertThat(commands).startsWith(//
        new ReferencingCommand(getOffCommand(REF), mplContinue.getMode(), false,
            mplContinue.getNeedsRedstone(), innerEntry - (beforeContinue + 1)), //
        new ReferencingCommand(getOffCommand(REF), true, outerEntry - (beforeContinue + 2)), //
        new ReferencingCommand(getOnCommand(REF), true, outerEntry - (beforeContinue + 3))//
    );
  }

}
