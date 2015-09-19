import java.util.*;

public class Document
{
	//MAP DATA STRUCTURE FOR LIST OF TOKENS COUNTS 
	public Map< String, Integer > tokens;
	
	//DOCUMENT CLASS
	public String data_category;

	public Document ( )
	{
		tokens = new HashMap<>();
	}
}
