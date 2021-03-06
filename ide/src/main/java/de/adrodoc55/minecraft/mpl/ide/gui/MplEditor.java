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
package de.adrodoc55.minecraft.mpl.ide.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.AncestorEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;

import org.beanfabrics.IModelProvider;
import org.beanfabrics.Link;
import org.beanfabrics.ModelProvider;
import org.beanfabrics.ModelSubscriber;
import org.beanfabrics.Path;
import org.beanfabrics.View;
import org.beanfabrics.swing.BnTextPane;

import de.adrodoc55.commons.AncestorAdapter;
import de.adrodoc55.commons.FileUtils;
import de.adrodoc55.minecraft.mpl.ide.autocompletion.AutoCompletion;
import de.adrodoc55.minecraft.mpl.ide.autocompletion.AutoCompletionAction;
import de.adrodoc55.minecraft.mpl.ide.gui.MplSyntaxFilterPM.CompilerExceptionWrapper;
import de.adrodoc55.minecraft.mpl.ide.gui.dialog.WindowControler;
import de.adrodoc55.minecraft.mpl.ide.gui.dialog.WindowView;
import de.adrodoc55.minecraft.mpl.ide.gui.dialog.autocompletion.AutoCompletionDialog;
import de.adrodoc55.minecraft.mpl.ide.gui.dialog.autocompletion.AutoCompletionDialogControler;
import de.adrodoc55.minecraft.mpl.ide.gui.dialog.autocompletion.AutoCompletionDialogPM.Context;
import de.adrodoc55.minecraft.mpl.ide.gui.dialog.hover.HoverDialogControler;
import de.adrodoc55.minecraft.mpl.ide.gui.editor.BnEditorTextPane;
import de.adrodoc55.minecraft.mpl.ide.gui.editor.EditorPM;
import de.adrodoc55.minecraft.mpl.ide.gui.editor.UndoableBnStyledDocument;
import de.adrodoc55.minecraft.mpl.ide.gui.utils.TextLineNumber;

/**
 * The MplEditor is a {@link View} on a {@link MplEditorPM}.
 *
 * @author Adrodoc55
 * @created by the Beanfabrics Component Wizard, www.beanfabrics.org
 */
public class MplEditor extends JComponent implements View<MplEditorPM>, ModelSubscriber {
  private static final long serialVersionUID = 1L;

  private static JFileChooser chooser;

  private static JFileChooser getFileChooser() {
    if (chooser == null) {
      chooser = new JFileChooser();
    }
    return chooser;
  }

  public static JFileChooser getDirChooser() {
    JFileChooser chooser = getFileChooser();
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setFileFilter(null);
    FileFilter filter = getFileFilter();
    chooser.removeChoosableFileFilter(filter);
    return chooser;
  }

  public static JFileChooser getMplChooser() {
    JFileChooser chooser = getFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    FileFilter filter = getFileFilter();
    chooser.setFileFilter(filter);
    chooser.addChoosableFileFilter(filter);
    return chooser;
  }

  private static FileFilter filter;

  private static FileFilter getFileFilter() {
    if (filter == null) {
      filter = new FileNameExtensionFilter("Minecraft Programming Language", new String[] {"mpl"});
    }
    return filter;
  }

  private final Link link = new Link(this);
  private ModelProvider localModelProvider;
  private JScrollPane scrollPane;
  private BnEditorTextPane textPane;
  private MplSyntaxFilter mplSyntaxFilter;
  private TextLineNumber textLineNumber;

  private List<WindowControler<?, ?>> ctrl = new ArrayList<>();
  private HoverDialogControler hoverCtrl = new HoverDialogControler();
  private AutoCompletionDialogControler autoCtrl = new AutoCompletionDialogControler(new Context() {
    @Override
    public void choose(AutoCompletionAction action) {
      action.performOn(getTextPane());
    }
  });

  /**
   * Constructs a new <code>MplEditor</code>.
   */
  public MplEditor() {
    setLayout(new BorderLayout());
    add(getScrollPane(), BorderLayout.CENTER);

    // Add listeners to dispose the temporary dialogs
    ctrl.add(autoCtrl);
    ctrl.add(hoverCtrl);
    for (WindowControler<?, ?> controler : ctrl) {
      addAncestorListener(new AncestorAdapter() {
        @Override
        public void ancestorMoved(AncestorEvent event) {
          controler.dispose();
        }
      });
      getTextPane().addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          controler.dispose();
        }
      });
      getTextPane().addFocusListener(new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
          if (!controler.hasView())
            return;
          WindowView<?> view = controler.getView();
          Component opposite = e.getOppositeComponent();
          if (opposite == null || (!view.equals(opposite)
              && !view.equals(SwingUtilities.getWindowAncestor(opposite)))) {
            controler.dispose();
          }
        }
      });
    }
  }

  /**
   * Returns the local {@link ModelProvider} for this class.
   *
   * @return the local <code>ModelProvider</code>
   * @wbp.nonvisual location=10,340
   */
  protected ModelProvider getLocalModelProvider() {
    if (localModelProvider == null) {
      localModelProvider = new ModelProvider(); // @wb:location=10,430
      localModelProvider.setPresentationModelType(MplEditorPM.class);
    }
    return localModelProvider;
  }

  /** {@inheritDoc} */
  public MplEditorPM getPresentationModel() {
    return getLocalModelProvider().getPresentationModel();
  }

  /** {@inheritDoc} */
  public void setPresentationModel(MplEditorPM pModel) {
    getLocalModelProvider().setPresentationModel(pModel);
  }

  /** {@inheritDoc} */
  public IModelProvider getModelProvider() {
    return this.link.getModelProvider();
  }

  /** {@inheritDoc} */
  public void setModelProvider(IModelProvider modelProvider) {
    this.link.setModelProvider(modelProvider);
  }

  /** {@inheritDoc} */
  public Path getPath() {
    return this.link.getPath();
  }

  /** {@inheritDoc} */
  public void setPath(Path path) {
    this.link.setPath(path);
  }

  public boolean isConnected() {
    return getPresentationModel() != null;
  }

  private JScrollPane getScrollPane() {
    if (scrollPane == null) {
      scrollPane = new JScrollPane();
      scrollPane.setRowHeaderView(getTextLineNumber());
      JPanel viewPortView = new JPanel(new BorderLayout());
      viewPortView.add(getTextPane(), BorderLayout.CENTER);
      scrollPane.setViewportView(viewPortView);
      scrollPane.getVerticalScrollBar().setUnitIncrement(10);
    }
    return scrollPane;
  }

  private TextLineNumber getTextLineNumber() {
    if (textLineNumber == null) {
      textLineNumber = new TextLineNumber(getTextPane());
    }
    return textLineNumber;
  }

  BnEditorTextPane getTextPane() {
    if (textPane == null) {
      textPane = new BnEditorTextPane(new BnEditorTextPane.Context() {
        @Override
        public EditorPM getPresentationModel() {
          return MplEditor.this.getPresentationModel();
        }
      });
      textPane.setPath(new Path("this.code"));
      textPane.setModelProvider(getLocalModelProvider());
      UndoableBnStyledDocument doc = textPane.getDocument();
      doc.setDocumentFilter(getMplSyntaxFilter());
      int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

      // Comment
      textPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_K, ctrl), "comment");
      textPane.getActionMap().put("comment", new AbstractAction() {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          int dot = textPane.getCaret().getDot();
          int mark = textPane.getCaret().getMark();
          doc.submit(new CommentUndoableEdit(doc, dot, mark));
        }
      });

      // Search and Replace
      textPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F, ctrl), "search and replace");
      textPane.getActionMap().put("search and replace", new AbstractAction() {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          if (isConnected()) {
            getPresentationModel().searchAndReplace();
          }
        }
      });

      // AutoCompletion
      textPane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ctrl), "auto complete");
      textPane.getActionMap().put("auto complete", new AbstractAction() {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          Caret caret = textPane.getCaret();
          Point caretPos = caret.getMagicCaretPosition();
          if (caretPos == null) {
            return;
          }
          autoCtrl.setOptions(getAutoCompletionOptions());
          autoCtrl.setLocation(textPane, caretPos.getLocation());
          autoCtrl.getView().setVisible(true);
          textPane.requestFocus();
        }
      });

      textPane.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent evt) {
          AutoCompletionDialog view = autoCtrl.getView();
          if (view.isVisible()) {
            switch (evt.getKeyCode()) {
              case KeyEvent.VK_ESCAPE:
              case KeyEvent.VK_DOWN:
              case KeyEvent.VK_UP:
              case KeyEvent.VK_ENTER:
                view.getBnList().dispatchEvent(evt);
                evt.consume();
                break;
              default:
                SwingUtilities.invokeLater(() -> autoCtrl.setOptions(getAutoCompletionOptions()));
            }
          }
        }
      });

      // Hover
      textPane.addMouseMotionListener(new MouseMotionAdapter() {
        CompilerExceptionWrapper lastEx;

        @Override
        public void mouseMoved(MouseEvent e) {
          Point point = e.getPoint();
          CompilerExceptionWrapper ex = getEx(point);
          if (lastEx == ex) {
            return;
          } else if (lastEx != null) {
            lastEx = null;
            hoverCtrl.dispose();
          }
          if (ex == null) {
            return;
          }
          Timer t = new Timer(200, a -> {
            Point secondPoint = textPane.getMousePosition();
            if (secondPoint != null && secondPoint.equals(point)) {
              lastEx = ex;
              showHoverDialog();
            }
          });
          t.setRepeats(false);
          t.start();
        }

        private CompilerExceptionWrapper getEx(Point point) {
          int offset = getTextPane().viewToModel(point);
          MplSyntaxFilterPM pModel = getMplSyntaxFilter().getPresentationModel();
          if (pModel == null) {
            return null;
          }
          List<CompilerExceptionWrapper> exs = pModel.getExceptions();
          if (exs == null) {
            return null;
          }
          for (CompilerExceptionWrapper ex : exs) {
            if (ex.getStartIndex() <= offset && offset < ex.getStopIndex()) {
              return ex;
            }
          }
          return null;
        }

        private void showHoverDialog() {
          hoverCtrl.setMessage(lastEx.getMessage());
          Rectangle pos;
          try {
            pos = getTextPane().modelToView(lastEx.getStartIndex());
          } catch (BadLocationException blex) {
            throw new UndeclaredThrowableException(blex);
          }
          Point location = new Point(pos.x, pos.y);
          hoverCtrl.setLocation(textPane, location);
          hoverCtrl.getView().setVisible(true);
          textPane.requestFocus();
        }
      });
    }
    return textPane;
  }

  public List<AutoCompletionAction> getAutoCompletionOptions() {
    BnTextPane textPane = getTextPane();
    Caret caret = textPane.getCaret();
    int index = Math.min(caret.getDot(), caret.getMark());
    String text = FileUtils.toUnixLineEnding(textPane.getText());
    return AutoCompletion.getOptions(index, text);
  }

  /**
   * @wbp.nonvisual location=16,379
   */
  private MplSyntaxFilter getMplSyntaxFilter() {
    if (mplSyntaxFilter == null) {
      mplSyntaxFilter = new MplSyntaxFilter();
      Path path = new Path("this.syntaxFilter");
      mplSyntaxFilter.setPath(path);
      mplSyntaxFilter.setModelProvider(getLocalModelProvider());
    }
    return mplSyntaxFilter;
  }

  public void discardAllEdits() {
    getTextPane().getUndoManager().discardAllEdits();
  }

}
