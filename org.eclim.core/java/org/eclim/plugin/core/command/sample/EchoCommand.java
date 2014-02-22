package org.eclim.plugin.core.command.sample;

import java.util.HashMap;

import org.eclim.annotation.Command;
import org.eclim.command.CommandLine;
import org.eclim.command.Options;
import org.eclim.plugin.core.command.AbstractCommand;
import org.eclim.plugin.core.util.ProjectUtils;

import org.eclipse.core.resources.IProject;

@Command(
    name = "echo",
    options =
        "REQUIRED p project ARG," +
        "REQUIRED f file ARG," +
        "REQUIRED o offset ARG," +
        "OPTIONAL e encoding ARG"
)
public class EchoCommand extends AbstractCommand {

    @Override
    public Object execute(CommandLine commandLine) throws Exception {
        String projectName = commandLine.getValue(Options.PROJECT_OPTION);
        String file = commandLine.getValue(Options.FILE_OPTION);

        IProject project = ProjectUtils.getProject(projectName);

        // translates client supplied byte offset to a character offset using the
        // 'project', 'file', 'offset', and 'encoding' command line args.
        int offset = getOffset(commandLine);

        HashMap<String,Object> result = new HashMap<String,Object>();
        result.put("project", ProjectUtils.getPath(project));
        result.put("file", ProjectUtils.getFilePath(project, file));
        result.put("offset", offset);
        if (commandLine.hasOption(Options.ENCODING_OPTION)){
          result.put("encoding", commandLine.getValue(Options.ENCODING_OPTION));
        }

        return result;
    }

}
