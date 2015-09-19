
import java.util.*;

// CLASS FOR MULTINOMIAL BAYES CLASSIFIER
public class NaiveBayes
{
	// P-VALUE = 0.001
	private double criticalVal = 10.83;

	private NaiveBayesKnowledgeBase knowledgeBase;

	public NaiveBayes ( NaiveBayesKnowledgeBase knowledgeBase )
	{
		this.knowledgeBase = knowledgeBase;
	}
	public NaiveBayes ( )
	{
		this( null );
	}
	public NaiveBayesKnowledgeBase getKnowledgeBase()
	{
		return knowledgeBase;
	}

	public double getChisquareCriticalValue()
	{
		return criticalVal;
	}

	public void setChisquareCriticalValue( double criticalValue )
	{
		this.criticalVal = criticalValue;
	}

	private List< Document > preproData( Map< String, String[] > trainData )
	{
		List< Document > dataset = new ArrayList<>();

		Document doc;
		
		String[] examples;

		String cat;

		Iterator< Map.Entry< String, String[] >> iterator = trainData.entrySet().iterator();

		// FOR ALL CATEGORIES AND EXAMPLES
		while ( iterator.hasNext() )
		{
			Map.Entry< String, String[] > e = iterator.next();
			cat = e.getKey();
			examples = e.getValue();

			for ( int i = 0; i < examples.length; ++i )
			{
				// TOKENIZATION
				doc = TextTokenizer.t( examples[ i ] );
				doc.data_category = cat;
				dataset.add( doc );
			}
		}

		return dataset;
	}

	private FeatureStats selectFeatures( List< Document > dataset )
	{
		FeatureExtraction extractor = new FeatureExtraction();
		FeatureStats sta = extractor.extractFeatureStats( dataset ); 
		Map< String, Double > selectedFeatures = extractor.chisquareStatistic( sta, criticalVal );
		Iterator< Map.Entry< String, Map< String, Integer >>> iterator = sta.jointcount.entrySet().iterator();
		while ( iterator.hasNext() )
		{
			String feature = iterator.next().getKey();

			if ( selectedFeatures.containsKey( feature ) == false )
			{
				iterator.remove();
			}
		}

		return sta;
	}

	public void training( Map< String, String[] > trainingDataset, Map< String, Double > catPriors ) throws IllegalArgumentException
	{
		List< Document > ds = preproData( trainingDataset );
		FeatureStats featureStats = selectFeatures( ds );
		
		// INITIALIZING
		knowledgeBase = new NaiveBayesKnowledgeBase();
		knowledgeBase.n = featureStats.n; 
		knowledgeBase.d = featureStats.jointcount.size();


		// CHECKING PRIOR PROBABILITIES
		if ( catPriors == null )
		{
			knowledgeBase.c = featureStats.counts_cate.size();
			knowledgeBase.logPriors = new HashMap<>();

			String cate;
			int c;
			for ( Map.Entry< String, Integer > entry : featureStats.counts_cate.entrySet() )
			{
				cate = entry.getKey();
				c = entry.getValue();

				knowledgeBase.logPriors.put( cate, Math.log( ( double ) c / knowledgeBase.n ) );
			}
		}
		else
		{
			knowledgeBase.c = catPriors.size();
			if ( knowledgeBase.c != featureStats.counts_cate.size() )
			{
				throw new IllegalArgumentException( "Something went wrong" );
			}

			String category;
			Double priorProb;
			for ( Map.Entry< String, Double > e : catPriors.entrySet() )
			{
				category = e.getKey();
				priorProb = e.getValue();
				if ( priorProb == null )
				{
					throw new IllegalArgumentException( "Something went wrong" );
				}
				else if ( priorProb < 0 || priorProb > 1 )
				{
					throw new IllegalArgumentException( "Something went wrong" );
				}

				knowledgeBase.logPriors.put( category, Math.log( priorProb ) );
			}
		}

		// SMOOTHING BY ADDING 1 TO AVOID UNDERFLOW
		Map< String, Double > occu = new HashMap<>();

		Integer occur;
		Double sum;
		for ( String category : knowledgeBase.logPriors.keySet() )
		{
			sum = 0.0;
			for ( Map< String, Integer > categoryListOccurrences : featureStats.jointcount.values() )
			{
				occur = categoryListOccurrences.get( category );
				if ( occur != null )
				{
					sum += occur;
				}
			}
			occu.put( category, sum );
		}

		// LOG LIKELIHOOD ESTIMATION
		String f;
		
		
		Integer c;
		double llh;
		Map< String, Integer > counts;
		
		for ( String cate : knowledgeBase.logPriors.keySet() )
		{
			for ( Map.Entry< String, Map< String, Integer >> entry : featureStats.jointcount.entrySet() )
			{
				f = entry.getKey();
				counts = entry.getValue();

				c = counts.get( cate );
				if ( c == null )
				{
					c = 0;
				}

				llh = Math.log( (c + 1.0) / (occu.get( cate ) + knowledgeBase.d) );
				if ( knowledgeBase.logLikelihoods.containsKey( f ) == false )
				{
					knowledgeBase.logLikelihoods.put( f, new HashMap< String, Double >() );
				}
				knowledgeBase.logLikelihoods.get( f ).put( cate, llh );
			}
		}
		occu = null;
	}

	public void traindataset( Map< String, String[] > trainds )
	{
		training( trainds, null );
	}

	public String predictValue( String text ) throws IllegalArgumentException
	{
		if ( knowledgeBase == null )
		{
			throw new IllegalArgumentException( "Something went wrong" );
		}

		Document doc = TextTokenizer.t( text );

		String cate;
		
		Integer occu;
		Double max = Double.NEGATIVE_INFINITY;
		Double logprob;
		String feature;
		String maxScoreCat = null;
		

		for ( Map.Entry< String, Double > entry1 : knowledgeBase.logPriors.entrySet() )
		{
			cate = entry1.getKey();
			logprob = entry1.getValue();

			for ( Map.Entry< String, Integer > entry2 : doc.tokens.entrySet() )
			{
				feature = entry2.getKey();

				if ( !knowledgeBase.logLikelihoods.containsKey( feature ) )
				{
					continue;
				}

				occu = entry2.getValue();

				logprob += occu * knowledgeBase.logLikelihoods.get( feature ).get( cate ); // multiply loglikelihood score with occurrences
			}

			if ( logprob > max )
			{
				max = logprob;
				maxScoreCat = cate;
			}
		}
		
		//RETURNING CATEGORY WITH HIGHEST SCORES
		return maxScoreCat;
	}
}
