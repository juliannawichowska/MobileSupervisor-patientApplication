package com.example.mobilesupervisor_patientapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;


public class HeartRateActivity extends AppCompatActivity {
    private static final String TAG = "HRRESULT";
    int period = SmartbandActivity.period;
    public List<Integer> hrvalue = new ArrayList<>();
    public List <String> hrdate = new ArrayList<>();
    Date date1;
    Date date = new Date();
    TextView text;
    LineChartView lineChartView;
    String label;

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hr);
        text = findViewById(R.id.textView6);
        date = Calendar.getInstance().getTime();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Results");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    for (DataSnapshot dsnap : ds.getChildren()) {
                        HeartRateModel hr = dsnap.getValue(HeartRateModel.class);
                            try {
                                Log.e(TAG, "value " + hr.getPulse());
                                date1 = new SimpleDateFormat("yyyy-MM-dd HH-mm").parse(hr.Date);
                            } catch (ParseException e) {
                                continue;
                            }
                                long duration = date.getTime() - date1.getTime();
                                if (period == 1) {
                                    label = "Ostatnie 30 minut";
                                    long MAX_DURATION = MILLISECONDS.convert(30, MINUTES);
                                    if (duration > MAX_DURATION) {
                                        Log.e(TAG, "value1111111 ");
                                        continue;
                                    } else {
                                        Log.e(TAG, "value1111111 " + hr.getPulse());
                                        hrvalue.add(hr.getPulse());
                                        hrdate.add(hr.getDate());
                                    }
                                } else if (period == 2) {
                                    label = "Ostatnia godzina";
                                    long MAX_DURATION = MILLISECONDS.convert(1, HOURS);
                                    if (duration > MAX_DURATION) {
                                        Log.e(TAG, "value1111111 ");
                                        continue;
                                    } else {
                                        Log.e(TAG, "value1111111 " + hr.getPulse());
                                        hrvalue.add(hr.getPulse());
                                        hrdate.add(hr.getDate());
                                    }
                                } else if (period == 3) {
                                    label = "Ostatni dzień";
                                    long MAX_DURATION = MILLISECONDS.convert(24, HOURS);
                                    if (duration > MAX_DURATION) {
                                        Log.e(TAG, "value1111111 ");
                                        continue;
                                    } else {
                                        Log.e(TAG, "value1111111 " + hr.getPulse());
                                        hrvalue.add(hr.Pulse);
                                        hrdate.add(hr.getDate());
                                    }
                                } else if (period == 4) {
                                    label = "Ostatni tydzień";
                                    long MAX_DURATION = MILLISECONDS.convert(7, DAYS);
                                    if (duration > MAX_DURATION) {
                                        Log.e(TAG, "value1111111 ");
                                        continue;
                                    } else {
                                        Log.e(TAG, "value1111111 " + hr.getPulse());
                                        hrvalue.add(hr.Pulse);
                                        hrdate.add(hr.getDate());
                                    }
                                } else if (period == 5) {
                                    label = "Ostatni miesiąc";
                                    long MAX_DURATION = MILLISECONDS.convert(30, DAYS);
                                    if (duration > MAX_DURATION) {
                                        Log.e(TAG, "value1111111 ");
                                        continue;
                                    } else {
                                        Log.e(TAG, "value1111111 " + hr.getPulse());
                                        hrvalue.add(hr.Pulse);
                                        hrdate.add(hr.getDate());
                                    }

                                }
                    }
                }
                drawChart(hrvalue, hrdate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error occured");
            }
        });
    }

    public void drawChart(List<Integer> hrvalue, List<String> hrdate) {
        lineChartView = findViewById(R.id.chart);
        List yAxisValues = new ArrayList();
        List axisValues = new ArrayList();
        List bottoms = new ArrayList();
        Line line = new Line(yAxisValues).setColor(Color.parseColor("#9C27B0"));
        int[] hrvalue_array = new int[hrvalue.size()];
        String[] hrdate_array = new String[hrdate.size()];
        for (int i = 0; i < hrvalue.size(); i++) {
            hrvalue_array[i] = hrvalue.get(i);
            Log.e(TAG, "WARTOSC"+hrvalue_array[i]);
        }
        for (int i = 0; i < hrdate.size(); i++) {
            hrdate_array[i] = hrdate.get(i);
            Log.e(TAG, "ZMIENNA"+hrdate_array[i]);
        }
        for(int i = 0; i < hrdate_array.length; i++){
            axisValues.add(i, new AxisValue(i).setLabel(hrdate_array[i]));
        }

        for (int i = 0; i < hrvalue_array.length; i++){
            yAxisValues.add(new PointValue(i, hrvalue_array[i]));
        }

        List lines = new ArrayList();
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);
        Axis axis = new Axis();
        axis.setValues(axisValues);
        axis.setTextSize(16);
        axis.setTextColor(Color.parseColor("#03A9F4"));
        data.setAxisXBottom(axis);
        Axis yAxis = new Axis();
        yAxis.setName("Puls");
        yAxis.setTextColor(Color.parseColor("#03A9F4"));
        yAxis.setTextSize(16);
        data.setAxisYLeft(yAxis);
        lineChartView.setLineChartData(data);
        Viewport viewport = new Viewport(lineChartView.getMaximumViewport());
        viewport.top = 110;
        lineChartView.setMaximumViewport(viewport);
        lineChartView.setCurrentViewport(viewport);
    }
}
