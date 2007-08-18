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
package org.eclim.command;

import org.apache.commons.cli.ParseException;

import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;

import org.eclim.Services;

import org.eclipse.swt.widgets.EclimDisplay;

/**
 * Entry point for client invocation.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class Main
{
  private static final Logger logger = Logger.getLogger(Main.class);

  /**
   * Main method for executing the client.
   *
   * @param _args The command line args.
   */
  public static final void main (String[] _args)
  {
    try{
      logger.debug("Main - enter");

      // set dummy display's current thread
      ((EclimDisplay)org.eclipse.swt.widgets.Display.getDefault())
        .setThread(Thread.currentThread());

      CommandLine commandLine = null;
      Options options = new Options();
      try{
        commandLine = options.parse(_args);
      }catch(ParseException e){
        System.out.println(
            Services.getMessage(e.getClass().getName(), e.getMessage()));
        logger.debug("Main - exit on error");
        System.exit(1);
      }

      if(commandLine.hasOption(Options.HELP_OPTION)){
        options.usage(commandLine.getValue(Options.HELP_OPTION));
        logger.debug("Main - exit");
      }else{
        String commandName = commandLine.getValue(Options.COMMAND_OPTION);
        logger.debug("Main - command: {}", commandName);
        if(commandName == null || commandName.trim().equals(StringUtils.EMPTY)){
          throw new IllegalArgumentException(
              Services.getMessage("command.required"));
        }
        Command command = Services.getCommand(commandName);

        String result = command.execute(commandLine);
        System.out.println(result);
      }
    }catch(Exception e){
      e.printStackTrace();

      logger.debug("Main - exit on error");
      System.exit(1);
    }
    logger.debug("Main - exit");
  }
}
