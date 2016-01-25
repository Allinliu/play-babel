package play.modules.babel;

public class BabelCompilationException extends RuntimeException {

	public String description;

	public BabelCompilationException(String message, String description) {
		super(message);
		this.description = description;
	}

}
