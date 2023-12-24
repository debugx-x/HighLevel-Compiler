package inputHandler;

/** Value object for holding a character and its location in the input text.
 *  Contains delegates to select character operations.
 *
 */
public class LocatedChar implements Locator {
	Character character;
	TextLocation location;
	
	public LocatedChar(Character character, TextLocation location) {
		super();
		this.character = character;
		this.location = location;
	}

	
	//////////////////////////////////////////////////////////////////////////////
	// getters
	
	public Character getCharacter() {
		return character;
	}
	public TextLocation getLocation() {
		return location;
	}
	public boolean isChar(char c) {
		return character == c;
	}
	
	
	
	//////////////////////////////////////////////////////////////////////////////
	// toString
	
	public String toString() {
		return "(" + charString() + ", " + location + ")";
	}
	private String charString() {
		if(Character.isWhitespace(character)) {
			int i = character;
			return String.format("'\\%d'", i);
		}
		else {
			return character.toString();
		}
	}

	
	//////////////////////////////////////////////////////////////////////////////
	// delegates
	// function to if the character is a string
	public boolean isString() {
		return character == '"';
	}

	// function to check if the character is a comment
	public boolean isComment() {
		return character == '#';
	}

	// function to check if the character is newline
	public boolean isNewline() {
		return character == '\n';
	}
	
	// function to check if the character is newline
	public boolean isSlash() {
		return character == '\\';
	}
	public boolean isN() {
		return character == 'n';
	}
	
	// function to check if the character is ascii
	public boolean isASCII() {
		// check if the character lies between decimal 32 and 126
		int i = character;
		return ((i >= 32) && (i <= 126));
	}
	
	public boolean isUpperCase() {
		return Character.isUpperCase(character);
	}
	public boolean isUnderScore() {
		return character == '_';
	}
	public boolean isAtSign() {
		return character == '@';
	}
	public boolean isLowerCase() {
		return Character.isLowerCase(character);
	}
	public boolean isDigit() {
		return Character.isDigit(character);
	}
	public boolean isWhitespace() {
		return Character.isWhitespace(character);
	}
}
