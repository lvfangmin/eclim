/**
 * Copyright (c) 2005 - 2006
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclim.plugin.jdt.command.src;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.command.filter.ErrorFilter;

import org.eclim.plugin.jdt.util.JavaUtils;

import org.eclim.util.ProjectUtils;

import org.eclim.util.file.FileOffsets;

import org.eclipse.jdt.core.ICompilationUnit;

import org.eclipse.jdt.core.compiler.IProblem;

/**
 * Command that updates the requested java src file.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class SrcUpdateCommand
  extends AbstractCommand
{
  /**
   * {@inheritDoc}
   */
  public String execute (CommandLine _commandLine)
    throws Exception
  {
    String file = _commandLine.getValue(Options.FILE_OPTION);
    String projectName = _commandLine.getValue(Options.PROJECT_OPTION);

    // only refresh the file.
    if(!_commandLine.hasOption(Options.VALIDATE_OPTION)){
      // getting the file will refresh it.
      ProjectUtils.getFile(projectName, file);

    // validate the src file.
    }else{
      // JavaUtils refreshes the file when getting it.
      ICompilationUnit src = JavaUtils.getCompilationUnit(projectName, file);

      IProblem[] problems = JavaUtils.getProblems(src);

      ArrayList<Error> errors = new ArrayList<Error>();
      String filename = src.getResource().getRawLocation().toOSString();
      FileOffsets offsets = FileOffsets.compile(filename);
      for(int ii = 0; ii < problems.length; ii++){
        int[] lineColumn =
          offsets.offsetToLineColumn(problems[ii].getSourceStart());

        // one day vim might support ability to mark the offending text.
        /*int[] endLineColumn =
          offsets.offsetToLineColumn(problems[ii].getSourceEnd());*/

        errors.add(new Error(
            problems[ii].getMessage(),
            filename,
            lineColumn[0],
            lineColumn[1],
            problems[ii].isWarning()));
      }

      return ErrorFilter.instance.filter(_commandLine, errors);
    }
    return StringUtils.EMPTY;
  }
}
