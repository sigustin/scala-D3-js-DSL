package lib.plot

import scala.collection.mutable.{ArrayBuffer, Stack}
import scala.scalajs.js

trait Section {
    val index: Int
    val label: String
    val jsObject: js.Any
}

/////////////////////////////////////////////
// THIS CANNOT WORK WITH THIS ARCHITECTURE //
/////////////////////////////////////////////

/**
  * Used in $RelationPlot
  * Allows to focus sections of a plot (i.e. groups)
  * and set what actions should be applied when a section is focused and when to unfocus it
  * (we called this 'focusing' instead of 'selecting' to avoid confusion with $d3.select
  */
class Focus {
    private var focusedSections = Stack[Section]()
    // When $unfocusOnRefocus is true, an already focused section will unfocused when it is focused again
    var unfocusOnRefocus = true

    // Functions to call on focus of a section (iff the focus works)
    private val functionsOnFocus = ArrayBuffer[() => Unit]()
    // Function to call to remove sections from the focused sections:
    // They should take one or several sections to remove from the focused ones
    // and return a section to push back in (or None) /!\ this could loop forever
    private var cleanFunction_1arg: Option[Section => Option[Section]] = None
    private var cleanFunction_2args: Option[(Section, Section) => Option[Section]] = None

    /** Resets $this to the default value */
    def reset(): Unit = {
        focusedSections.clear()
        unfocusOnRefocus = true
        functionsOnFocus.clear()
        cleanFunction_1arg = None
        cleanFunction_2args = None
    }

    def focus(section: Section): Unit = {
        if (!focusedSections.contains(section)) {
            functionsOnFocus.foreach(_())
            // Call the function to unfocus (if you can)
            if (cleanFunction_1arg.isDefined) {
                val newSectionToFocus = (cleanFunction_1arg.get)(section)
                if (newSectionToFocus.isDefined)
                    focus(newSectionToFocus.get)
                return
            }
            else if (cleanFunction_2args.isDefined && focusedSections.nonEmpty) {
                val newSectionToFocus = (cleanFunction_2args.get)(focusedSections.pop(), section)
                if (newSectionToFocus.isDefined)
                    focus(newSectionToFocus.get)
                return
            }
            // otherwise focus the section
            focusedSections.push(section)
        }
        else if (unfocusOnRefocus)
            focusedSections.filter(! _.equals(section))
    }

    def unfocus(section: Section): Unit = focusedSections.filter(! _.equals(section))

    /** Adds a new function to call on the focus of a section */
    def onFocus(f: () => Unit): Unit = functionsOnFocus += f

    /** Setters for the function to call on focus to remove sections from the focus */
    def cleanFunction_=(f: Section => Option[Section]): Unit = {
        cleanFunction_1arg = Some(f)
        if (cleanFunction_2args.isDefined)
            cleanFunction_2args = None
    }
    def cleanFunction_=(f: (Section, Section) => Option[Section]): Unit = {
        cleanFunction_2args = Some(f)
        if (cleanFunction_1arg.isDefined)
            cleanFunction_1arg = None
    }
}
