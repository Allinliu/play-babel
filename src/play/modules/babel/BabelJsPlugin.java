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

public class BabelJsPlugin extends PlayPlugin {

	public static final String babelPath = Play.configuration.getProperty("babel.path", "");
	public static final boolean precompiling = System.getProperty("precompile") != null;
	public static final String tmpOrPrecompile = Play.usePrecompiled || precompiling ? "precompiled" : "tmp";
	public static final File compiledDir = Play.getFile(tmpOrPrecompile + Play.configuration.getProperty("babel.js.compiled_directory_path", "/js-modules"));
	public static final File sourceDir = Play.getFile(Play.configuration.getProperty("babel.js.source_directory_path", "/public/javascripts/modules"));

	public static File getCompiledFile(File componentFile) {
		return new File(compiledDir, componentFile.getAbsolutePath().replace(sourceDir.getAbsolutePath(), ""));
	}

	public static File getCompiledFile(String moduleName) {
		return new File(compiledDir, moduleName + ".js");
	}

	public static File getSourceFile(String moduleName) {
		return new File(sourceDir, moduleName + ".js");
	}

	public static void compile(File sourceFile) {

		if (!compiledDir.exists()) {
			compiledDir.mkdirs();
		}

		List<String> command = new ArrayList<String>();
		if (sourceFile == null) {
			command.add(babelPath);
			command.add(sourceDir.getAbsolutePath());
			command.add("--out-dir");
			command.add(compiledDir.getAbsolutePath());
		}
		else {

			File compiledFile = getCompiledFile(sourceFile);
			command.add(babelPath);
			command.add(sourceFile.getAbsolutePath());
			command.add("--out-file");
			command.add(compiledFile.getAbsolutePath());
		}

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
				throw new BabelCompilationException("Babel compilation error", processErrors);
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
		compile(null);
		Logger.info("Done.");
	}

}
