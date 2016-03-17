/*
 * MPL (Minecraft Programming Language): A language for easy development of commandblock
 * applications including and IDE.
 *
 * © Copyright (C) 2016 Adrodoc55
 *
 * This file is part of MPL (Minecraft Programming Language).
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
 * MPL (Minecraft Programming Language): Eine Sprache für die einfache Entwicklung von Commandoblock
 * Anwendungen, beinhaltet eine IDE.
 *
 * © Copyright (C) 2016 Adrodoc55
 *
 * Diese Datei ist Teil von MPL (Minecraft Programming Language).
 *
 * MPL ist Freie Software: Sie können es unter den Bedingungen der GNU General Public License, wie
 * von der Free Software Foundation, Version 3 der Lizenz oder (nach Ihrer Wahl) jeder späteren
 * veröffentlichten Version, weiterverbreiten und/oder modifizieren.
 *
 * MPL wird in der Hoffnung, dass es nützlich sein wird, aber OHNE JEDE GEWÄHRLEISTUNG,
 * bereitgestellt; sogar ohne die implizite Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN
 * BESTIMMTEN ZWECK. Siehe die GNU General Public License für weitere Details.
 *
 * Sie sollten eine Kopie der GNU General Public License zusammen mit MPL erhalten haben. Wenn
 * nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.adrodoc55.minecraft.mpl.compilation;

import java.io.File;
import java.nio.charset.CharacterCodingException;

import de.adrodoc55.commons.FileUtils;

/**
 * @author Adrodoc55
 */
public class CompilerException extends Exception {

  private static final long serialVersionUID = 1L;

  private MplSource source;

  public CompilerException(MplSource source, String message) {
    super(message);
    init(source);
  }

  public CompilerException(MplSource source, String message, Throwable cause) {
    super(message, cause);
    init(source);
  }

  private void init(MplSource source) {
    this.source = source;
  }

  public MplSource getSource() {
    return source;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    String path = FileUtils.getCanonicalPath(source.file);
    sb.append(path).append(':').append(source.token.getLine()).append(":\n");
    sb.append(this.getLocalizedMessage()).append("\n");
    sb.append(source.line).append("\n");
    int count = source.token.getCharPositionInLine();
    sb.append(new String(new char[count]).replace('\0', ' '));
    sb.append("^");
    Throwable cause = getCause();
    if (cause != null) {
      sb.append("\ncause: ");
      File file = null;
      if (cause instanceof FileException) {
        file = ((FileException) cause).getFile();
        cause = cause.getCause();
      }
      if (cause != null && cause instanceof CharacterCodingException) {
        sb.append("Invalid file encoding, must be UTF-8!");
      } else {
        sb.append(cause.toString());
      }
      if (file != null) {
        sb.append(" In file: ").append(FileUtils.getCanonicalPath(file));
      }
    }
    return sb.toString();
  }
}
