package samophis.kunou.main.test;

import com.jsoniter.JsonIterator;
import com.jsoniter.output.EncodingMode;
import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.DecodingMode;
import samophis.kunou.core.modules.ModuleLoader;
import samophis.kunou.main.modules.impl.ApplicationModule;

public class Main {
    public static void main(String... args) {
        try {
            JsonIterator.setMode(DecodingMode.DYNAMIC_MODE_AND_MATCH_FIELD_WITH_HASH);
            JsonStream.setMode(EncodingMode.DYNAMIC_MODE);
            ModuleLoader loader = ModuleLoader.newInstance();
            ApplicationModule module = ApplicationModule.getInstance(loader);
            loader.addModule(module);
            loader.startModule(module);
        } catch (Throwable thr) {
            thr.printStackTrace();
        }
    }
}