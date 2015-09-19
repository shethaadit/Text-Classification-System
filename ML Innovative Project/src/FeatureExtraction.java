import java.util.*;

//CLASS FOR FEATURE STATS OBJECTS GENERATION
public class FeatureExtraction
{

	public FeatureStats extractFeatureStats( List< Document > data )
	{
		FeatureStats sta = new FeatureStats();

		Integer data_category_c;
		Map< String, Integer > counts_feature_cat;
		String cate;
		
		String feat;
		Integer count_feature;
		for ( Document d : data )
		{
			//NUMBER OF OBSERVATIONS
			++sta.n;
			cate = d.data_category;
			data_category_c = sta.counts_cate.get( cate );
			if ( data_category_c == null )
			{
				sta.counts_cate.put( cate, 1 );
			}
			else
			{
				sta.counts_cate.put( cate, data_category_c + 1 );
			}

			for ( Map.Entry< String, Integer > entry : d.tokens.entrySet() )
			{
				feat = entry.getKey();

				// GETTING FEATURE COUNTS
				counts_feature_cat = sta.jointcount.get( feat );
				if ( counts_feature_cat == null )
				{
					// INITIALIZING
					sta.jointcount.put( feat, new HashMap< String, Integer >() );
				}

				count_feature = sta.jointcount.get( feat ).get( cate );
				if ( count_feature == null )
				{
					count_feature = 0;
				}
				
				// INCREASING NUMBER OF OCCURRENCES
				sta.jointcount.get( feat ).put( cate, ++count_feature );
			}
		}

		return sta;
	}

	// CHI SQAURE FEATURE SELECTION METHOD
	public Map< String, Double > chisquareStatistic( FeatureStats stats, double criticalLevel )
	{
		int N1d, N0d, N00, N01, N10, N11;
		Map< String, Double > selFeatures = new HashMap<>();

		String f;
		
		Map< String, Integer > list_cat;

		String cat;
		double score;
		Double prescore;
		for ( Map.Entry< String, Map< String, Integer >> entry1 : stats.jointcount.entrySet() )
		{
			f = entry1.getKey();
			list_cat = entry1.getValue();

			N1d = 0;
			for ( Integer count : list_cat.values() )
			{
				N1d += count;
			}

		
			N0d = stats.n - N1d;

			for ( Map.Entry< String, Integer > entry2 : list_cat.entrySet() )
			{
				cat = entry2.getKey();
				
				// FEATURES BELONG TO THE SPECIFIC CATEGORY PARAMETERS
				N11 = entry2.getValue(); 
				N01 = stats.counts_cate.get( cat ) - N11; 

				N00 = N0d - N01; 
				N10 = N1d - N11; 
				
				//CALCULATING CHI-SQUARE SCORE
				score = stats.n * Math.pow( N11 * N00 - N10 * N01, 2 ) / ((N11 + N01) * (N11 + N10) * (N10 + N00) * (N01 + N00));

				// IF IT IS LARGER THAN THRESHOLD THEN ADDING
				if ( score >= criticalLevel )
				{
					prescore = selFeatures.get( f );
					if ( prescore == null || score > prescore )
					{
						selFeatures.put( f, score );
					}
				}
			}
		}

		return selFeatures;
	}
}
