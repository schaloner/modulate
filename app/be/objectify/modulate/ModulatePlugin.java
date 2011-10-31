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
