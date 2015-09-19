import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class TextClassifier
{
	public static String s = null;
	public static String s1 = null;
	HashSet< String > stopwords = new HashSet< String >();
	HashMap< String, String > classNameMap = new HashMap< String, String >();
	HashMap< String, String > testClassNameMap = new HashMap< String, String >();
	NaiveBayes nb = new NaiveBayes();

	public static void main( String[] args ) throws IOException
	{
		s = args[ 0 ];
		s1 = args[1];
		TextClassifier obj = new TextClassifier();
		obj.stopWords();
		obj.getClasses();
		obj.readFromTrain();
		obj.testClassNames();
		obj.readFromTest();
	}

	private void getClasses() throws IOException
	{
		BufferedReader br = new BufferedReader( new FileReader( s + "\\class_name.txt" ) );
		String line;

		while ( (line = br.readLine()) != null )
		{
			//SPLITTING WORDS
			String[] tokens = line.split( "\\s+" ); 
			if ( tokens != null && tokens.length > 0 )
				classNameMap.put( tokens[ 0 ], tokens[ 1 ] );
		}
		br.close();
	}

	private void stopWords() throws IOException
	{
		System.out.println( "Folder Name: " + s );
		System.out.println("Please wait while Training & Testing.......");
		
		System.out.println();
		BufferedReader br = new BufferedReader( new FileReader( s + "\\stopwords.txt" ) );
		String line;

		while ( (line = br.readLine()) != null )
		{
			String[] tokens = line.split( "," );
			for ( String token : tokens )
				stopwords.add( token );
		}
		br.close();
	}

	private void readFromTrain() throws NumberFormatException, IOException
	{
		Map< String, String[] > trainingExamples = new HashMap<>();
		for ( Map.Entry< String, String > entry : classNameMap.entrySet() )
		{
			String classFolderName = entry.getValue();
			File classFolder = new File( s + "\\train\\" + classFolderName );
			File[] listofFiles = classFolder.listFiles();
			List< String > lines = new ArrayList< String >();

			for ( File file : listofFiles )
			{
				Scanner in = new Scanner( file );
				StringBuffer sb = new StringBuffer();
				while ( in.hasNextLine() )
				{
					String[] tokens = in.nextLine().replaceAll( "[']", "" ).replaceAll( "[^a-zA-Z]", " " ).split( "\\s+" );
					for ( int i = 0; i < tokens.length; i++ )
					{
						String token = tokens[ i ].toLowerCase();
						if ( !token.isEmpty() && !stopwords.contains( token ) )
							sb.append( token ).append( " " );
					}
				}
				lines.add( sb.toString() );
				in.close();
			}
			trainingExamples.put( entry.getKey(), lines.toArray( new String[ lines.size() ] ) );
		}
		this.nb.traindataset( trainingExamples );
		NaiveBayesKnowledgeBase knowledgeBase = nb.getKnowledgeBase();
		this.nb = new NaiveBayes( knowledgeBase );
	}

	private void testClassNames() throws NumberFormatException, IOException
	{
		BufferedReader br = new BufferedReader( new FileReader( s + "\\dev_label.txt" ) );
		String line;

		while ( (line = br.readLine()) != null )
		{
			String[] tokens = line.split( "\\s+" ); // Split words by space
			if ( tokens != null && tokens.length > 0 )
				testClassNameMap.put( tokens[ 0 ], tokens[ 1 ] );
		}
		br.close();
	}

	private void readFromTest() throws NumberFormatException, IOException
	{
		int total = 0;
		File result_file = new File( s, s1 + ".txt" );
		result_file.createNewFile();
		FileWriter fw = new FileWriter( result_file.getAbsoluteFile() );
		BufferedWriter bw = new BufferedWriter( fw );

		for ( Map.Entry< String, String > entry : testClassNameMap.entrySet() )
		{
			String testFileName = entry.getKey();
			File file = new File( s + "\\dev\\" + testFileName );
			Scanner in = new Scanner( file );
			StringBuffer sb = new StringBuffer();
			while ( in.hasNextLine() )
			{
				String[] tokens = in.nextLine().replaceAll( "[']", "" ).replaceAll( "[^a-zA-Z]", " " ).split( "\\s+" );
				for ( int i = 0; i < tokens.length; i++ )
				{
					String token = tokens[ i ].toLowerCase();
					if ( !token.isEmpty() && !stopwords.contains( token ) )
						sb.append( token ).append( " " );
				}
			}
			in.close();
			
			String outputEn = this.nb.predictValue( sb.toString() );
			bw.write( testFileName + " ");
			bw.write( outputEn + "\n");

			if ( outputEn.equals( entry.getValue() ) )
				total++;
		}

		DecimalFormat df = new DecimalFormat( "#.##" );

		System.out.println("********************************************");
		System.out.println( "Total number of matched files after testing = " + total );
		System.out.println( "Total number of test files = " + testClassNameMap.size() );
		System.out.println();
		System.out.println( "**********************************************************" );
		System.out.println();

		float accuracy = ( float ) total / ( float ) testClassNameMap.size();
		
		//PRINTING ACCURACY UP TO 2 DECIMAL DIGITS
		System.out.println( "Accuracy of classifier is: " + df.format(accuracy * 100) + " %" );

		System.out.println();

		System.out.println( "Finsihed writing output result to file - " + s + "//" + " "+ s1 + ".txt" );
		bw.close();
	}
}
