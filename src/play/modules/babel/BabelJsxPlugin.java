package play.modules.babel;

import play.Logger;
import play.Play;
import play.PlayPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This plugin intercepts requests for static files ending in '.jsx', and
 * serves the compiled javascript instead.
 */
public class BabelJsxPlugin extends PlayPlugin {

	public static final boolean precompiling = System.getProperty("precompile") != null;
	public static final String tmpOrPrecompile = Play.usePrecompiled || precompiling ? "precompiled" : "tmp";
	public static final File compiledDir = Play.getFile(tmpOrPrecompile +  "/components");
	public static final File componentsDir = Play.getFile("/public/javascripts/components");

	public static void compileAll() {
		String babelFullpath = Play.configuration.getProperty("babel.path", "");
		List<String> command = new ArrayList<String>();
		command.add(babelFullpath);
		command.add("--presets");
		command.add("react");
		command.add(componentsDir.getAbsolutePath());
		command.add("--out-dir");
		command.add(compiledDir.getAbsolutePath());

		ProcessBuilder pb = new ProcessBuilder(command);
		Process babelProcess = null;
		try {
			babelProcess = pb.start();

			BufferedReader minifyReader = new BufferedReader(new InputStreamReader(babelProcess.getInputStream()));
			String line;
			while ((line = minifyReader.readLine()) != null) {
				Logger.info("%s", line);
			}

			String processErrors = "";
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(babelProcess.getErrorStream()));
			while ((line = errorReader.readLine()) != null) {
				processErrors += line + "\n";
			}
			if (!processErrors.isEmpty()) {
				Logger.error("%s", processErrors);
				throw new RuntimeException("Babel compilation error");
			}
			minifyReader.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (babelProcess != null) {
				babelProcess.destroy();
			}
		}
	}

	@Override
	public void onLoad() {
		Logger.info("Compile all react jsx files...");
		compileAll();
		Logger.info("Done.");
	}

}