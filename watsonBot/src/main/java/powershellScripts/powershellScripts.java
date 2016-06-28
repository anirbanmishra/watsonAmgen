package powershellScripts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class powershellScripts {
    @SuppressWarnings("deprecation")
	public void clearCache() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        File f=new File("C:/Users/amishr02/Downloads/Clear-IECachedData.ps1");
        URL url = f.toURL();
        InputStream in = url.openStream();
        String home = System.getProperty("user.home");
        String download_home=home+"/Downloads/clearCache.ps1";
        download_home=download_home.replace("\\", "/");
        Files.copy(in, Paths.get(download_home), StandardCopyOption.REPLACE_EXISTING);
        in.close();
        Process proc;    
        String command="powershell "+download_home;
        proc = runtime.exec(command);
        proc.getOutputStream().close();
    }
}