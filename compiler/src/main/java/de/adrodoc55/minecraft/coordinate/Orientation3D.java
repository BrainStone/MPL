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
package de.adrodoc55.minecraft.coordinate;

import static de.adrodoc55.minecraft.coordinate.Direction3D.EAST;
import static de.adrodoc55.minecraft.coordinate.Direction3D.SOUTH;
import static de.adrodoc55.minecraft.coordinate.Direction3D.UP;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.Immutable;

import org.antlr.v4.runtime.Token;

import com.google.common.base.Preconditions;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.karneim.pojobuilder.GenerateMplPojoBuilder;

/**
 * @author Adrodoc55
 */
@Immutable
@EqualsAndHashCode(exclude = "token")
@Getter
public class Orientation3D {
  private final Token token;
  private Direction3D a;
  private Direction3D b;
  private Direction3D c;

  public Orientation3D() {
    this(EAST, UP, SOUTH);
  }

  @GenerateMplPojoBuilder
  public Orientation3D(Direction3D a, Direction3D b, Direction3D c) {
    this.token = null;
    Preconditions.checkNotNull(a, "a == null!");
    Preconditions.checkNotNull(b, "b == null!");
    Preconditions.checkNotNull(c, "c == null!");
    setValue(a, b, c);
  }

  public Orientation3D(String def) {
    this(def, null);
  }

  public Orientation3D(String def, Token token) {
    this.token = token;
    Preconditions.checkNotNull(def, "def == null!");
    char[] defArray = def.toCharArray();
    List<Direction3D> r = new ArrayList<>(3);
    for (int i = 0; i < defArray.length; i++) {
      boolean negative = false;
      if (defArray[i] == '-') {
        negative = true;
        i++;
      }
      if (i >= defArray.length) {
        throw new IllegalArgumentException("Every '-' must be followed by an axis!");
      }
      char a = defArray[i];
      Axis3D axis;
      try {
        axis = Axis3D.valueOf(String.valueOf(Character.toUpperCase(a)));
      } catch (IllegalArgumentException ex) {
        throw new IllegalArgumentException(
            "Unknown direction '" + (negative ? "-" : "") + a + "'!");
      }
      Direction3D direction = Direction3D.valueOf(axis, negative);
      r.add(direction);
    }
    if (r.size() != 3) {
      throw new IllegalArgumentException("An orientation must contain 3 directions!");
    }
    setValue(r.get(0), r.get(1), r.get(2));
  }

  private void setValue(Direction3D a, Direction3D b, Direction3D c) {
    Axis3D aAxis = a.getAxis();
    Axis3D bAxis = b.getAxis();
    Axis3D cAxis = c.getAxis();
    if (aAxis == bAxis || bAxis == cAxis || cAxis == aAxis) {
      throw new IllegalArgumentException("All directions must be on different axis!");
    }
    this.a = a;
    this.b = b;
    this.c = c;
  }

  /**
   * @return <b>a</b> - the primary direction of this orientation
   */
  public Direction3D getA() {
    return a;
  }

  /**
   * @return <b>b</b> - the secondary direction of this orientation
   */
  public Direction3D getB() {
    return b;
  }

  /**
   * @return <b>c</b> - the tertiary direction of this orientation
   */
  public Direction3D getC() {
    return c;
  }

  public Direction3D get(Axis3D axis) {
    Preconditions.checkNotNull(axis, "axis == null!");
    if (a.getAxis() == axis)
      return a;
    if (b.getAxis() == axis)
      return b;
    if (c.getAxis() == axis)
      return c;
    throw new InternalError(
        "A, B and C must be on different axis and there are only 3 axis. Therefor this can never happen!");
  }

}
