package com.example.dht2;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private String tempC = "0.00", tempF = "0.00", humi = "0.00";
    private String Time1 = "0.00";

    private TextView mRed, mgreen, mblue;

    private DatabaseReference databaseReference;
    private FireFox fireFox = new FireFox();
    private LineChart chart;
    private TextView tempCText;
    private TextView tempFText;
    private TextView humiText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        chart = (LineChart) findViewById(R.id.chart);
        tempCText = (TextView) findViewById(R.id.txtTempC);
        tempFText = (TextView) findViewById(R.id.txtTempF);
        humiText = (TextView)findViewById(R.id.txtHumi);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        chart.setDrawGridBackground(false);
        chart.getDescription().setEnabled(false);
        chart.setData(new LineData());
        chart.setDragEnabled(true);
        chart.setPinchZoom(true);

        Legend l = chart.getLegend();

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setLabelCount(6, true);
        leftAxis.setMaxWidth(1.0f);
        leftAxis.setMinWidth(0.5f);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setGranularity(0.2f);

        chart.invalidate();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot fireFoxSnapshot :dataSnapshot.getChildren()){
                    if (dataSnapshot.exists()) {
                        fireFox = fireFoxSnapshot.getValue(FireFox.class);
                        if (fireFox != null) {
                            tempC = fireFox.getCel();
                            tempF = fireFox.getFar();
                            humi = fireFox.getHumidity();
                            Time1 = fireFox.getTime1();
                            if (Time1 != null) {
                                if (Time1.equals("2.00")) {
                                    removeLastEntry();
                                    removeDataSet();
                                    chart.setData(new LineData());
                                    chart.invalidate();
                                }
                                if (Float.parseFloat(Time1) > 3.0f && Float.parseFloat(Time1) < 20.0f) {
                                    addEntry(Float.parseFloat(Time1), Float.parseFloat(tempC), Float.parseFloat(humi));
                                    tempCText.setText(tempC);
                                    tempFText.setText(tempF);
                                    humiText.setText(humi);
                                } else {
                                    removeLastEntry();
                                    removeDataSet();
                                    chart.setData(new LineData());
                                    chart.invalidate();
                                }
                            }
                        }
                    } else {
                        fireFox = fireFoxSnapshot.getValue(FireFox.class);
                    }
                }

            }

            @Override
            public void onCancelled (DatabaseError databaseError) {
            }

        });
    }

    int [] mColor = ColorTemplate.VORDIPLOM_COLORS;

    private void  addEntry(float x, float y ,float m){
        LineData data = chart.getData();
        ILineDataSet set = data.getDataSetByIndex(0),
                set1 = data.getDataSetByIndex(1);
        if(set ==null ){
            set = createSet("溫度變化");
            data.addDataSet(set);
            setColorConfig(set, 131, 127, 133);

        }

        if(set == null){
            set1 = createSet("濕度變化");
            data.addDataSet(set1);
            setColorConfig(set1, 0, 188, 13);
        }
        data.addEntry(new Entry(x,y),0);
        data.addEntry(new Entry(x,m),1);

        data.notifyDataChanged();

        chart.notifyDataSetChanged();
        chart.setVisibleXRangeMaximum(6);
        chart.moveViewTo(data.getEntryCount() -7,50f, YAxis.AxisDependency.RIGHT);
    }

    private void    removeLastEntry(){
        LineData data = chart.getData();
        if(data == null){
            ILineDataSet set = data.getDataSetByIndex(0);
            ILineDataSet set1 = data.getDataSetByIndex(1);

            if(set != null && set!= null){
                Entry e = set.getEntryForXValue(set.getEntryCount() -1, Float.NaN);
                Entry e1 = set1.getEntryForXValue(set1.getEntryCount() -1, Float.NaN);

                data.removeEntry(e, 0);
                data.removeEntry(e1,1);

                data.notifyDataChanged();
                chart.notifyDataSetChanged();
                chart.invalidate();
            }
        }
    }

    private LineDataSet createSet(String str){
        LineDataSet set = new LineDataSet(null,str);
        return set;
    }

    private void   setColorConfig(ILineDataSet s, int r, int g, int b){
        ((LineDataSet)s).setLineWidth(2f);
        ((LineDataSet)s).setCircleRadius(4.5F);
        ((LineDataSet)s).setColor(Color.rgb(r,g,b));
        ((LineDataSet)s).setCircleColor(Color.rgb(240,99,99));
        ((LineDataSet)s).setHighLightColor(Color.rgb(190,190,190));
        s.setAxisDependency(YAxis.AxisDependency.LEFT);
        s.setValueTextSize(10f);
        ((LineDataSet)s).setMode(LineDataSet.Mode.CUBIC_BEZIER);
    }

    private LineDataSet addDataSet(float x,float y){
//
        LineData data = chart.getData();
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        LineDataSet set1 = new LineDataSet(yVals, "");
        int count = 0;

        if(data != null){
            count = (data.getDataSetCount()+1);

            yVals.add(new Entry(x, y));

            set1 = new LineDataSet(yVals, "溫度變化");
            set1.setLineWidth(2f);
            set1.setCircleRadius(4.5f);

            int color = mColor[count % mColor.length];

            set1.setValueTextColor(color);
            set1.setLineWidth(2f);
            set1.setCircleRadius(4.5f);
            set1.setColor(Color.rgb(0, 188, 13));
            set1.setCircleColor(Color.rgb(24, 32, 232));
            set1.setHighLightColor(Color.rgb(190, 190, 190));
            set1.setAxisDependency(YAxis.AxisDependency.LEFT);
            set1.setValueTextSize(10f);
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            data.addDataSet(set1);
            data.notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.invalidate();
        }
        return  set1;

    }

    private void removeDataSet(){
        LineData data = chart.getData();

        if(data != null){
            for(int i= data.getDataSetCount()-1;i>=0;i--) {
                data.removeDataSet(data.getDataSetByIndex(i));
            }

            chart.notifyDataSetChanged();
            chart.invalidate();
        }
    }





}
