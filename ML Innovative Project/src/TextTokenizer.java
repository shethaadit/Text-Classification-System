import java.util.*;

//CLASS FOR TOKENIZING TEXTS
public class TextTokenizer
{
	
	public static String[] extract( String text )
	{
		return text.split( " " );
	}
	
	public static String temp( String text )
	{
		return text.replaceAll( "\\p{P}", " " ).replaceAll( "\\s+", " " ).toLowerCase( Locale.getDefault() );
	}
	
	

	public static Map< String, Integer > getcou( String[] keyword )
	{
		Map< String, Integer > counts = new HashMap<>();

		Integer count;
		for ( int i = 0; i < keyword.length; ++i )
		{
			count = counts.get( keyword[ i ] );
			if ( count == null )
			{
				count = 0;
			}
			counts.put( keyword[ i ], ++count ); // increase counter for the keyword
		}

		return counts;
	}
	public static Document t( String text )
	{
		String preprocessedText = temp( text );
		String[] keywordArray = extract( preprocessedText );

		Document doc = new Document();
		doc.tokens = getcou( keywordArray );
		return doc;
	}
}
