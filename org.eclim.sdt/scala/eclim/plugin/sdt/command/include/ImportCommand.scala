package eclim.plugin.sdt.command.include

import eclim.plugin.sdt.util.ScalaUtils
import org.eclim.annotation.Command
import org.eclim.command.CommandLine
import org.eclim.command.Options
import org.eclim.plugin.core.command.AbstractCommand
import org.eclim.plugin.core.util.ProjectUtils
import org.eclim.util.file.Position
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.search.IJavaSearchConstants
import org.eclipse.jdt.core.search.SearchEngine
import org.eclipse.jdt.core.search.TypeNameMatch
import org.eclipse.jdt.internal.corext.util.TypeNameMatchCollector
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.TextUtilities
import org.eclipse.text.edits.MultiTextEdit
import scala.collection.JavaConversions

/**
 * Command which is used to auto-import depedency.
 *
 * @author Fangmin Lv
 */
@Command(
  name = "scala_import",
  options = "REQUIRED p project ARG,REQUIRED f file ARG,REQUIRED o offset ARG,REQUIRED e encoding ARG,REQUIRED t missingType ARG, OPTIONAL v value ARG"
)
class ImportCommand extends AbstractCommand
{
  override def execute(commandLine: CommandLine): Object = {
    val project = commandLine.getValue(Options.PROJECT_OPTION)
    val file = commandLine.getValue(Options.FILE_OPTION)
    val offset = getOffset(commandLine)
    val missingType = commandLine.getValue(Options.TYPE_OPTION)
    val value = commandLine.getValue(Options.VALUE_OPTION)
    val src = ScalaUtils.getSourceFile(project, file)

    if (value != null) {
      val edits = new MultiTextEdit()
      val dlenPre = ProjectUtils.getDocument(project, file).getLength()
      val cont = updateDoc(ProjectUtils.getDocument(project, file), value)
      val dlenAfter = ProjectUtils.getDocument(project, file).getLength()

      if (src.isWorkingCopy()) {
        src.commitWorkingCopy(true, null)
      }

      src.save(null, true)

      Position.fromOffset(ProjectUtils.getFilePath(project, file), null, offset, 0)

    } else {
      val resultCollector = new java.util.ArrayList[TypeNameMatch]
      val scope = SearchEngine.createJavaSearchScope(Array[IJavaElement](src.getJavaProject))
      val typesToSearch = Array(missingType.toArray)
      new SearchEngine().searchAllTypeNames(null, typesToSearch, scope, new TypeNameMatchCollector(resultCollector), IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, new NullProgressMonitor)
      val ret = JavaConversions.asScalaBuffer(resultCollector) map { typeFound => typeFound.getFullyQualifiedName() }
      JavaConversions.bufferAsJavaList(ret)
    }
  }

  private def updateDoc(document: IDocument, value: String): String = {
    val lineDelimiter = TextUtilities.getDefaultLineDelimiter(document)

    // Find the package declaration
    val text = document.get
    var insertIndex = 0
    val packageIndex = text.indexOf("package", insertIndex)
    var preInsert = ""

    if (packageIndex != -1) {
      // Insert on the line after the last package declaration, with a line of whitespace first if needed
      var nextLineIndex = text.indexOf(lineDelimiter, packageIndex) + 1
      var nextLineEndIndex = text.indexOf(lineDelimiter, nextLineIndex)
      var nextLine = text.substring(nextLineIndex, nextLineEndIndex).trim()

      // scan to see if package declaration is not multi-line
      while (nextLine.startsWith("package")) {
        nextLineIndex = text.indexOf(lineDelimiter, nextLineIndex) + 1
        nextLineEndIndex = text.indexOf(lineDelimiter, nextLineIndex)
        nextLine = text.substring(nextLineIndex, nextLineEndIndex).trim()
      }

      // Get the next line to see if it is already whitespace
      if (nextLine.trim() == "") {
        // This is a whitespace line, add the import here
        insertIndex = nextLineEndIndex + 1
      } else {
        // Need to insert whitespace after the package declaration and insert
        preInsert = lineDelimiter
        insertIndex = nextLineIndex
      }
    } else {
      // Insert at the top of the file
      insertIndex = 0
    }

    // Insert the import as the third line in the file... RISKY AS HELL :D
    document.replace(insertIndex, 0, preInsert + "import " + value + lineDelimiter)
    document.get
  }
}
