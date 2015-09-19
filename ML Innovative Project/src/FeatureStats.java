
import java.util.*;

// CLASS TO STORE FEATURE EXTRACTION OBJECTS
public class FeatureStats
{
	public int n;

	//MAP TO STORE CO-OCCURANCE OF FEATURE AND CATEGORY VALUES
	public Map< String, Map< String, Integer >> jointcount;

	// MAP TO STORE EACH CATEGORY IN TRAINING DATASET
	public Map< String, Integer > counts_cate;

	public FeatureStats ( )
	{
		n = 0;
		jointcount = new HashMap<>();
		counts_cate = new HashMap<>();
	}
}
