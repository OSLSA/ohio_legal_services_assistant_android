package org.seols.ohiolegalservicesassistant;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Created by joshuagoodwin on 2/15/16.
 */
public class RulesTableOfContentsFragment extends Fragment {

    TableLayout tl;
    String bookName;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.rules_all_books_layout, container, false);
        tl = (TableLayout) rootView.findViewById(R.id.rules_titles);
        bookName = getArguments().getString("bookName");
        setTitles(inflater);
        return rootView;
    }

    private void setTitles(LayoutInflater inflater) {

        String[] titles = getResources().getStringArray(getResources().getIdentifier(bookName + "_toc", "array", "org.seols.ohiolegalservicesassistant"));
            for (int i = 0; i < titles.length; i++){
                View tr = inflater.inflate(R.layout.rules_row, null);
                try {
                    double test = NumberFormat.getInstance().parse(titles[i]).doubleValue();
                    // if this is an int, should convert then to string otherwise string will end in .0
                    String ruleNumber = (test % 1 == 0) ? Integer.toString((int) test) : Double.toString(test);
                    ruleNumber =ruleNumber.replace('.', '_');
                    Book tag = new Book(ruleNumber, i);
                    tr.setTag(tag);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            //tr.setTag(tag);
            ((TextView)tr.findViewById(R.id.row_text)).setText(titles[i]);
            tr.setOnClickListener(myClickListener);
            tl.addView(tr);
        }
    }

    private View.OnClickListener myClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // create book object to get tag
            Book tag = (Book)v.getTag();

            // get information to pass
            String ruleNumber = tag.ruleNumber;
            int position = tag.rulePosition;

            // create bundle to push
            Bundle args = new Bundle();
            args.putString("ruleNumber", ruleNumber);
            args.putString("bookName", bookName);
            args.putInt("rulePosition", position);

            // change framents
            Fragment newFragment = new RulesDetailFragment();
            ((MainActivity)getActivity()).setFragment(newFragment, "RULE DETAIL", "Rule " + ruleNumber, args);
        }
    };

    /**
     * internal class just to set tag on text views
     */
    private class Book {
        String ruleNumber;
        int rulePosition;

        public Book (String ruleNumber, int rulePosition) {
            this.ruleNumber = ruleNumber;
            this.rulePosition = rulePosition;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set title
        String title = getArguments().getString("title");
        ((AppCompatActivity)getActivity()).getSupportActionBar()
                .setTitle(title);
    }
}
