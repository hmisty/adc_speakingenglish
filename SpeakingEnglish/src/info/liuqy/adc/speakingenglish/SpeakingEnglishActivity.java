package info.liuqy.adc.speakingenglish;

import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.os.Bundle;

public class SpeakingEnglishActivity extends ListActivity {
	
    private class DFA {
        private static final int INIT_STATE = 0, EXPR_TAG = 1, CN_TAG = 2, EN_TAG = 3, CN_TEXT = 4, EN_TEXT = 5, PRE_FINAL = 6, FINAL_STATE = 7;
        int currentState = 0;
        Map<Integer, Map<String, Integer>> T = new HashMap<Integer, Map<String, Integer>>();
        public DFA() {
            Map<String, Integer> m = new HashMap<String, Integer>();
            m.put("expression", EXPR_TAG);
            T.put(INIT_STATE, m);
            m = new HashMap<String, Integer>();
            m.put("cn", CN_TAG);
            m.put("en", EN_TAG);
            T.put(EXPR_TAG, m);
            m = new HashMap<String, Integer>();
            m.put("text", CN_TEXT);
            T.put(CN_TAG, m);
            m = new HashMap<String, Integer>();
            m.put("text", EN_TEXT);
            T.put(EN_TAG, m);
            m = new HashMap<String, Integer>();
            m.put("en", PRE_FINAL);
            T.put(CN_TEXT, m);
            m = new HashMap<String, Integer>();
            m.put("cn", PRE_FINAL);
            T.put(EN_TEXT, m);
            m = new HashMap<String, Integer>();
            m.put("text", FINAL_STATE);
            T.put(PRE_FINAL, m);
        }
        
        public void reset() {
            currentState = 0;
        }
        
        public int nextState(String symbol) {
            if (currentState != FINAL_STATE
                    && T.get(currentState).containsKey(symbol))
                currentState = T.get(currentState).get(symbol);
            return currentState;
        }
    }

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}