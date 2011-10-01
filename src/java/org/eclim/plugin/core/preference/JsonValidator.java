/**
 * Copyright (C) 2011  Eric Van Dewoestine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.eclim.plugin.core.preference;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * Option validator that validates that the option value is valid json.
 *
 * @author Eric Van Dewoestine
 */
public class JsonValidator
  implements Validator
{
  private static final Gson GSON = new Gson();

  private Class<?> type;
  private Validator itemValidator;

  public JsonValidator(Class<?> type, Validator itemValidator)
  {
    this.type = type;
    this.itemValidator = itemValidator;
  }

  /**
   * {@inheritDoc}
   * @see Validator#isValid(Object)
   */
  public boolean isValid(Object value)
  {
    if (value != null){
      try{
        Object result = GSON.fromJson((String)value, type);
        if (type.isArray() && itemValidator != null){
          Object[] results = (Object[])result;
          for(Object v : results){
            if (!itemValidator.isValid(v)){
              return false;
            }
          }
        }
      }catch(JsonParseException jpe){
        return false;
      }
    }
    return true;
  }

  /**
   * {@inheritDoc}
   * @see Validator#getMessage(String,Object)
   */
  public String getMessage(String name, Object value)
  {
    if (value != null) {
      try{
        Object result = GSON.fromJson((String)value, type);
        if (type.isArray() && itemValidator != null){
          Object[] results = (Object[])result;
          for(Object v : results){
            if (!itemValidator.isValid(v)){
              return itemValidator.getMessage(name, v);
            }
          }
        }
      }catch(JsonParseException jpe){
        Throwable cause = jpe.getCause();
        if (cause != null && cause.getMessage() != null){
          return cause.getMessage();
        }
        return jpe.getMessage();
      }
    }
    return null;
  }
}
