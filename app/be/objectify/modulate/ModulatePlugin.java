/*
 * Copyright 2009-2010 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.objectify.modulate;


import org.yaml.snakeyaml.Yaml;
import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.vfs.VirtualFile;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses the conf/modulate.yml file and adds any modules required for the framework ID.
 *
 * @author Steve Chaloner
 */
public class ModulatePlugin extends PlayPlugin
{
    @Override
    public void onLoad()
    {
        Logger.info("Starting Modulate...");
        VirtualFile modulateConf = Play.getVirtualFile("conf/modulate.yml");
        if (modulateConf.exists())
        {
            Yaml yaml = new Yaml();
            Object o = yaml.load(modulateConf.inputstream());

            if (o instanceof LinkedHashMap<?, ?>)
            {
                LinkedHashMap<Object, Map<?, ?>> objects = (LinkedHashMap<Object, Map<?, ?>>) o;

                List<String> modulatedModules = (List<String>)objects.get(Play.id);
                if (modulatedModules != null)
                {
                    for (String modulatedModulePath : modulatedModules)
                    {
                        File module = Play.getFile(modulatedModulePath);
                        String moduleName = module.getName();
                        if (moduleName.contains("-"))
                        {
                            moduleName = moduleName.substring(0, moduleName.indexOf("-"));
                        }
                        if (!Play.modules.containsKey(moduleName))
                        {
                            if (module.isDirectory())
                            {
                                Logger.info("Modulate: adding " + moduleName);
                                Play.addModule(moduleName,
                                               module);
                            }
                            else
                            {
                                Logger.error("Module %s will not be loaded because %s does not exist",
                                             moduleName,
                                             modulatedModulePath);
                            }
                        }
                    }
                }
            }
        }
        else
        {
            Logger.error("No modulate.yml file found in conf");
        }
        Logger.info("...Modulate finished");
    }
}
