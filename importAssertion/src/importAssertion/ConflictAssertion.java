package importAssertion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import importAssertion.Parser.Assertion;

public class ConflictAssertion {
    Assertion assertion;
    // Liste ist ein Tupel
    // Map <ColumName, Object>
    List<Map<String, Object>> dataset = new ArrayList<>();
	
    public ConflictAssertion( Assertion assertion){
    	this.assertion = assertion;
    }
	
    public void addListEntry(Map<String, Object> entry){
    	dataset.add(entry);
    }
    
}
