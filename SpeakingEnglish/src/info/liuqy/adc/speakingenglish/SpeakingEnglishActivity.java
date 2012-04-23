package info.liuqy.adc.speakingenglish;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SpeakingEnglishActivity extends ListActivity {

    public static final int RATING_ACTION = 0;
    public static final int EDIT_ACTION = 1;
    static final String ACTION = "info.liuqy.adc.easynote.EDIT";
    static final String TITLE = "title";
    static final String BODY = "body";

    List<String> cns = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    
	//all expressions cn => en
	Map<String, String> exprs = null;
	
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
        
        try {
            exprs = loadExpressionsFromXml(R.xml.cn2en);
        } catch (IOException e) {
            Toast.makeText(this, R.string.error_xml_file, Toast.LENGTH_SHORT);
        } catch (XmlPullParserException e) {
            Toast.makeText(this, R.string.error_parsing_xml, Toast.LENGTH_SHORT);
        }

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, cns);
        this.setListAdapter(adapter);

        for (String cn : exprs.keySet()) {
            cns.add(cn);
        }
//        adapter.notifyDataSetChanged();
        
        this.registerForContextMenu(getListView());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        TextView tv = (TextView)v;
        String text = tv.getText().toString();
        if (exprs.containsKey(text)) //Chinese displayed now
            tv.setText(exprs.get(text));
        else //English displayed now, refresh the display
            adapter.notifyDataSetChanged();
    }
    
    private Map<String, String> loadExpressionsFromXml(int resourceId) throws XmlPullParserException, IOException {
        Map<String, String> exprs = new HashMap<String, String>();
        XmlPullParser xpp = getResources().getXml(resourceId);
        DFA dfa = new DFA();
        String cn = null, en = null;
        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                dfa.nextState(xpp.getName());
            }
            else if (xpp.getEventType() == XmlPullParser.TEXT) {
                int state = dfa.nextState("text");
                if (state == DFA.CN_TEXT)
                    cn = xpp.getText();
                else if (state == DFA.EN_TEXT)
                    en = xpp.getText();
                else if (state == DFA.FINAL_STATE) {
                    if (cn == null)
                        cn = xpp.getText();
                    else if (en == null)
                        en = xpp.getText();
                    exprs.put(cn, en);
                    dfa.reset();
                    cn = en = null;
                }
            }
            xpp.next();
        }
        return exprs;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inf = this.getMenuInflater();
        inf.inflate(R.menu.opt_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.show_about) {
            Intent i = new Intent(this, AboutActivity.class);
            this.startActivityForResult(i, SpeakingEnglishActivity.RATING_ACTION);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == SpeakingEnglishActivity.RATING_ACTION
                && resultCode == SpeakingEnglishActivity.RESULT_OK) {
            float rating = data.getFloatExtra(AboutActivity.RATING, 0.0f);
            if (rating >= 3.0f) {
                Toast.makeText(this, String.format(getString(R.string.we_are_NB), rating), Toast.LENGTH_SHORT)
                .show();
            }
        } else if (requestCode == SpeakingEnglishActivity.EDIT_ACTION
                && resultCode == SpeakingEnglishActivity.RESULT_OK) {
            String cn = data.getStringExtra(TITLE);
            String en = data.getStringExtra(BODY);
            exprs.put(cn, en);
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        MenuInflater inf = this.getMenuInflater();
        inf.inflate(R.menu.ctx_menu, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.edit_todo) {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
            int idx = info.position;
            String cn = cns.get(idx);
            String en = exprs.get(cn);
            
            Intent i = new Intent(ACTION);
            i.putExtra(TITLE, cn);
            i.putExtra(BODY, en);
            startActivityForResult(i, EDIT_ACTION); //requestCode is only for the caller to distinguish the requests
        }
        return super.onContextItemSelected(item);
    }

}