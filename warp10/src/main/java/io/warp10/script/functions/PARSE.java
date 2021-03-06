//
//   Copyright 2016  Cityzen Data
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//

package io.warp10.script.functions;

import io.warp10.continuum.gts.GTSEncoder;
import io.warp10.continuum.gts.GTSHelper;
import io.warp10.continuum.gts.GeoTimeSerie;
import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStackFunction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse a string matching the format fed into the update endpoint
 */
public class PARSE extends NamedWarpScriptFunction implements WarpScriptStackFunction {
  
  public PARSE(String name) {
    super(name);
  }
  
  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    Object top = stack.pop();
    
    if (!(top instanceof String)) {
      throw new WarpScriptException(getName() + " expects a String on top of the stack.");
    }
    
    GTSEncoder encoder = null;
    GTSEncoder lastencoder = null;
    
    StringReader reader = new StringReader(top.toString());
    BufferedReader br = new BufferedReader(reader);
    
    List<GeoTimeSerie> series = new ArrayList<GeoTimeSerie>();

    try {
      while(true) {
        String line = br.readLine();
        
        if (null == line) {
          break;
        }
        
        encoder = GTSHelper.parse(lastencoder, line, null, null, Long.MAX_VALUE, true);
        
        if (null != lastencoder && lastencoder != encoder) {
          series.add(encoder.getDecoder(true).decode());
          lastencoder = encoder;
        } else {
          lastencoder = encoder;
        }
      }
      br.close();
    } catch (IOException ioe) {
      throw new WarpScriptException(ioe);
    } catch (ParseException pe) {
      throw new WarpScriptException(pe);
    }
    
    
    if (null != encoder) {
      series.add(encoder.getDecoder(true).decode());
    }
    
    stack.push(series);
    
    return stack;
  }
}
