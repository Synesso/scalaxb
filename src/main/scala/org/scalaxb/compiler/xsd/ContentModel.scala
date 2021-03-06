/*
 * Copyright (c) 2010 e.e d3si9n
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.scalaxb.compiler.xsd

import scala.xml._
import scala.util.regexp.WordExp
import scala.util.automata._

object ContentModel extends WordExp  {

  type _labelT = ElemRef;
  type _regexpT = RegExp;

  object Translator extends WordBerrySethi  {
    override val lang: ContentModel.this.type = ContentModel.this;
    import lang._ ;
  }

  case class ElemRef(name: String = "") extends Label {
    override def toString() = name
  }
  
  def fromSchema(nodes: Seq[Node]): Seq[RegExp] = 
    for (child <- nodes; if child.isInstanceOf[scala.xml.Elem])
      yield fromSchema(child)

  def fromSchema(node: Node): RegExp = node match {
    case <sequence>{ children @ _* }</sequence> => Sequ(fromSchema(filterElem(children)):_*);
    case <choice>{ children @ _* }</choice> =>  Alt(fromSchema(filterElem(children)):_*);
    case <group>{ children @ _* }</group> => Sequ(fromSchema(filterElem(children)):_*);
    case <element>{ children @ _* }</element>   =>
      if ((node \ "@name").text != "")
        Letter(ElemRef((node \ "@name").text))
      else
        Letter(ElemRef((node \ "@ref").text))
    case <complexContent>{ children @ _* }</complexContent>  => Letter(ElemRef())
    case <simpleContent>{ children @ _* }</simpleContent>  => Letter(ElemRef())
    case <extension>{ children @ _* }</extension>  => Letter(ElemRef())
    case _ => error("XSD ContentModel error:" + node.toString)
  }
  
  def filterElem(nodes: Seq[Node]) = {
    for (child <- nodes; if child.isInstanceOf[scala.xml.Elem])
      yield child  
  }
}

/*
sealed abstract class ContentModel ;
case class ELEMENTS(r:ContentModel.RegExp) extends ContentModel ;
case class MIXED(r:ContentModel.RegExp) extends ContentModel ;
case object SimpleContent extends ContentModel ;
*/
