package com.file.manager.Tools;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.file.manager.IOnBackPressed;
import com.file.manager.Activities.MainActivity;
import com.file.manager.R;
import com.file.manager.helpers.PermissionsHelper;
import com.file.manager.ui.Adapters.BenchmarkAdapter;
import com.file.manager.ui.Adapters.CpuInfoAdapter;
import com.file.manager.ui.Models.CopyProgressMonitor;
import com.file.manager.ui.Models.CpuModel;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.utils.CopyUtility;
import com.file.manager.ui.utils.CpuStats;
import com.file.manager.ui.utils.DateUtils;
import com.file.manager.ui.utils.DiskUtils;
import com.file.manager.ui.utils.Timer;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class StorageBenchmarkFragment extends Fragment implements  IOnBackPressed {

    private Fragment parent;
    private Map<String,Long> bufferMap=new LinkedHashMap<>();
    private Map<String,Long>bytesToWriteMap=new LinkedHashMap<>();
    private Map<String,Integer>threadCount=new LinkedHashMap<>();
    private Map<String,String>mountPoints=new LinkedHashMap<>();
    private ArrayList<CopyUtility>threads= new ArrayList<>();
    private ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
    private ActivityManager activityManager;
    private Timer timer;
    private GraphView graph;
    private TextView cpuLoad;
    private TextView usedRamInfo;
    private TextView availableRamInfo;
    private BenchmarkAdapter benchmarkAdapter;
    private CpuInfoAdapter cpuInfoAdapter;
    private CpuStats cpuStats=new CpuStats();
    private CustomFile destination;
    public StorageBenchmarkFragment(Fragment parent){
        this.parent=parent;
        this.bufferMap.put("4096bytes",4096L);
        this.bufferMap.put("500Kb",500* DiskUtils.SIZE_KB);
        this.bufferMap.put("1Mb",DiskUtils.SIZE_MB);
        this.bufferMap.put("2Mb",2*DiskUtils.SIZE_MB);
        this.bufferMap.put("5Mb",5*DiskUtils.SIZE_MB);
        this.bufferMap.put("10Mb",10*DiskUtils.SIZE_MB);

        this.bytesToWriteMap.put("100Mb",100*DiskUtils.SIZE_MB);
        this.bytesToWriteMap.put("200Mb",200*DiskUtils.SIZE_MB);
        this.bytesToWriteMap.put("300Mb",300*DiskUtils.SIZE_MB);
        this.bytesToWriteMap.put("500Mb",500*DiskUtils.SIZE_MB);
        this.bytesToWriteMap.put("1Gb",1*DiskUtils.SIZE_GB);
        this.bytesToWriteMap.put("2Gb",2*DiskUtils.SIZE_GB);
        this.bytesToWriteMap.put("3Gb",3*DiskUtils.SIZE_GB);
        this.bytesToWriteMap.put("4Gb",4*DiskUtils.SIZE_GB);
        for(int i=1;i<=15;i++){
        this.threadCount.put("#"+i,i);}
        File[]mounts=DiskUtils.getInstance().getStorageDirs();
        this.mountPoints.put("Internal",mounts[0].getPath());
        if(mounts.length>1&&mounts[1]!=null)
        this.mountPoints.put("External",mounts[1].getPath());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.benchmark_storage_fragment,container,false);
        ((MainActivity)getContext()).toolbar.setVisibility(View.GONE);
        final Button  benchmark=root.findViewById(R.id.start_benchmark);
        final Spinner bufferSpinner=root.findViewById(R.id.buffer);
        final Spinner byteSpinner=root.findViewById(R.id.byteSize);
        final Spinner threadSpinner=root.findViewById(R.id.threadCount);
        final Spinner mountSpinner=root.findViewById(R.id.mountPoint);
        final TextView speedTv=root.findViewById(R.id.speed);
        final TextView elapsedTv=root.findViewById(R.id.elapsed);
        final ProgressBar progressBar=root.findViewById(R.id.progressBar);
        final TextView bytesCopied=root.findViewById(R.id.bytesCopied);
        final RecyclerView threadList=root.findViewById(R.id.threadList);
        final RecyclerView cpuInfoList=root.findViewById(R.id.cpuInfo);
        final LinearLayoutManager manager1= new LinearLayoutManager(getContext());
        final LinearLayoutManager manager2= new LinearLayoutManager(getContext());
        graph=root.findViewById(R.id.graph);
        cpuLoad=root.findViewById(R.id.cpuLoad);
        usedRamInfo=root.findViewById(R.id.usedRamInfo);
        availableRamInfo=root.findViewById(R.id.availableRamInfo);
        manager1.setOrientation(RecyclerView.VERTICAL);
        manager2.setOrientation(RecyclerView.HORIZONTAL);
        threadList.setItemAnimator(null);
        cpuInfoList.setItemAnimator(null);
        threadList.setLayoutManager(manager1);
        cpuInfoList.setLayoutManager(manager2);
        // fetch cpu information
        getCpuInformation();
        cpuInfoAdapter=new CpuInfoAdapter(getContext(),cpuStats.getCpuInfo());
        cpuInfoList.setAdapter(cpuInfoAdapter);

        ArrayAdapter<CharSequence>bufferAdapter= new ArrayAdapter<CharSequence>(getContext(),R.layout.spinner_item_benchmark,getBufferArray());
        ArrayAdapter<CharSequence>byteAdapter= new ArrayAdapter<CharSequence>(getContext(),R.layout.spinner_item_benchmark,getByteArray());
        final ArrayAdapter<CharSequence>threadAdapter= new ArrayAdapter<CharSequence>(getContext(),R.layout.spinner_item_benchmark,getThreadArray());
        final ArrayAdapter<CharSequence>mountAdapter= new ArrayAdapter<CharSequence>(getContext(),R.layout.spinner_item_benchmark,getMountArray());

        bufferAdapter.setDropDownViewResource(R.layout.spinner_drop_down_item_benchmark);
        byteAdapter.setDropDownViewResource(R.layout.spinner_drop_down_item_benchmark);
        threadAdapter.setDropDownViewResource(R.layout.spinner_drop_down_item_benchmark);
        mountAdapter.setDropDownViewResource(R.layout.spinner_drop_down_item_benchmark);

        bufferSpinner.setAdapter(bufferAdapter);
        byteSpinner.setAdapter(byteAdapter);
        threadSpinner.setAdapter(threadAdapter);
        mountSpinner.setAdapter(mountAdapter);

        timer= new Timer();
        final String speed[]={" "};
        timer.setListener(new Timer.TimerListener() {
            @Override
            public void calculate(long seconds) {
                long workCompleted=0L;
                long workToBeDone=0L;
                long writeSpeed = 0;
                DiskUtils diskUtils=DiskUtils.getInstance();
                for(int i=0;i<threads.size();i++){
                    CopyUtility utility=threads.get(i);
                    workCompleted+=utility.getProgressMonitor().getWorkCompleted();
                    workToBeDone+=utility.getProgressMonitor().getWorkToBeDone();
                    writeSpeed = utility.getProgressMonitor().getWriteSpeedInt(seconds,workCompleted);
                    //update adapter here
                    benchmarkAdapter.setSeconds(seconds);
                    benchmarkAdapter.notifyItemChanged(i);
                }
                speed[0] = CopyProgressMonitor.getWriteSpeedFromBytes(writeSpeed);
                speedTv.setText(speed[0]);
                elapsedTv.setText("Elapsed: "+DateUtils.getHoursMinutesSec(seconds));
                progressBar.setProgress((int)(((float)workCompleted/(float)workToBeDone)*100));
                bytesCopied.setText(diskUtils.getSize(workCompleted)+"/"+diskUtils.getSize(workToBeDone));
                if(!anyThreadsAlive()){
                    benchmark.setText("START BENCHMARK");
                    benchmark.setBackgroundResource(R.drawable.drawable_highlight6);
                    timer.stop();
                    return;
                }
            }
        });
        benchmarkAdapter =new BenchmarkAdapter(getContext(),threads);
        threadList.setAdapter(benchmarkAdapter);
        benchmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              if(anyThreadsAlive()) {
                  stopThreads();
                  benchmark.setText("START BENCHMARK");
                  benchmark.setBackgroundResource(R.drawable.drawable_highlight6);
              }else {
                  threads.clear();
                  destination = new CustomFile(mountPoints.get(mountSpinner.getSelectedItem().toString()));

                  if(!checkStoragePermission(destination))
                      return;

                  long bytesToWrite = bytesToWriteMap.get(byteSpinner.getSelectedItem().toString());
                  long buffer = bufferMap.get(bufferSpinner.getSelectedItem().toString());
                  long count = threadCount.get(threadSpinner.getSelectedItem().toString());

                  //set up threads
                  for (int i = 0; i < count; i++) {
                      final CopyUtility utility = new CopyUtility(getContext(), CopyUtility.BENCHMARK, destination);
                      utility.setBenchBuffer(buffer);
                      utility.setBenchByteSize(bytesToWrite);
                      utility.setId(i);
                      threads.add(utility);
                      utility.setCopyListener(new CopyUtility.OnCopyListener() {
                          @Override
                          public void onSuccess(ArrayList<CustomFile> files) {
                              //   threads.remove(utility.getId());
                          }

                          @Override
                          public void onFailed(ArrayList<CustomFile> files) {
                              //  threads.remove(utility.getId());
                          }
                      });
                  }
               if (isSpaceAvailable(destination, bytesToWrite * count + DiskUtils.SIZE_MB * 100 * count)){
                      startThreads();
                      timer.start();
                   benchmark.setText("STOP BENCHMARK");
                   benchmark.setBackgroundResource(R.drawable.drawable_highlight8);
                   benchmarkAdapter.notifyDataSetChanged();
              }else {
                   threads.clear();
                   Toast.makeText(getContext(),"No enough storage space!",Toast.LENGTH_SHORT).show();
                   Toast.makeText(getContext(),"reduce byte size or thread count!",Toast.LENGTH_LONG).show();
               }

              }
            }
        });

        Toolbar toolbar =root.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        activityManager=(ActivityManager)getContext().getSystemService(Context.ACTIVITY_SERVICE);
        graph.setTitleColor(Color.WHITE);

        return root;
    }

    private boolean checkStoragePermission(CustomFile file){
        String storage=DiskUtils.getInstance().getStartDirectory(file);
        if(!file.canWrite()) {
            if (!PermissionsHelper.getInstance().uriValid(new File(storage), getContext())) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                ((MainActivity) getContext()).startActivityForResult(intent, 32);
                return false;
            }
        }
        return true;
    }

    public CustomFile getDestination() {
        return destination;
    }

    private void startThreads(){
      // start threads
      for(CopyUtility utility:threads)
          utility.Execute();
  }

  private void stopThreads(){
      // stop threads
      for(CopyUtility utility:threads) utility.cancel();
      threads.clear();
      timer.stop();
  }

  private boolean anyThreadsAlive(){
        for(CopyUtility utility:threads)
            if (utility.isRunning())
                return true;
        return false;
  }

  private boolean isSpaceAvailable(CustomFile file,long bytes){
        return DiskUtils.getInstance().isSpaceEnough(file,bytes);
  }
    private String[]getBufferArray(){
        String[]array= new String[bufferMap.size()];
        Iterator<Map.Entry<String,Long>>iterator=bufferMap.entrySet().iterator();
        for(int i=0;i<bufferMap.size();i++){
            array[i]=iterator.next().getKey();
        }
        return array;
    }
    private String[]getByteArray(){
        String[]array= new String[bytesToWriteMap.size()];
        Iterator<Map.Entry<String,Long>>iterator=bytesToWriteMap.entrySet().iterator();
        for(int i=0;i<bytesToWriteMap.size();i++){
            array[i]=iterator.next().getKey();
        }
        return array;
    }
    private String[]getThreadArray(){
        String[]array= new String[threadCount.size()];
        Iterator<Map.Entry<String,Integer>>iterator=threadCount.entrySet().iterator();
        for(int i=0;i<threadCount.size();i++){
            array[i]=iterator.next().getKey();
        }
        return array;
    }
    private String[]getMountArray(){
        String[]array= new String[mountPoints.size()];
        Iterator<Map.Entry<String,String>>iterator=mountPoints.entrySet().iterator();
        for(int i=0;i<mountPoints.size();i++){
            array[i]=iterator.next().getKey();
        }
        return array;
    }

    private void fetchRamMemoryInfo(){
        activityManager.getMemoryInfo(memoryInfo);
        String used=DiskUtils.getInstance().getSizeRounded(memoryInfo.totalMem-memoryInfo.availMem);
        String total=DiskUtils.getInstance().getSizeRounded(memoryInfo.totalMem);
        String available=DiskUtils.getInstance().getSizeRounded(memoryInfo.availMem);
        usedRamInfo.setText(used+"/"+total);
        availableRamInfo.setText(available);
    }

    private void shiftColor(TextView view,int percent){
      if(percent<=25){
          view.setTextColor(Color.argb(100,190,255,64));
      }else if(percent<=35){
          view.setTextColor(Color.argb(100,114,234,88));
      }else if(percent<=50){
          view.setTextColor(Color.argb(100,228,91,47));
      }else if(percent<=75){
          view.setTextColor(Color.argb(100,228,67,55));
      }else {
          view.setTextColor(Color.argb(100,227,44,30));
      }
    }

    private void getCpuInformation(){
        final Timer timer= new Timer();
        timer.setIntervals(1);
        final int average[]=new int[2];
        final DataPoint points[]= new DataPoint[60];
        for(int i=0;i<points.length;i++){
            points[i]=new DataPoint(i,0);
        }
        graph.addSeries(new LineGraphSeries(points));
        timer.setListener(new Timer.TimerListener() {
            @Override
            public void calculate(long seconds) {
                if(getContext()==null)
                    timer.stop();
                if(!cpuStats.isRunning()){
                    cpuInfoAdapter.notifyDataSetChanged();
                    cpuStats.execute();
                }
                int x=(int)(seconds%59);
                int count=0;

                for(CpuModel cpu:cpuStats.getCpuInfo()){
                    count+=cpu.getCurUsage();
                }
                if(count!=0&cpuStats.getCpuInfo().size()!=0)
                average[0]=count/cpuStats.getCpuInfo().size();
                points[x]=new DataPoint(x,average[1]);
                points[x+1]=new DataPoint(x+1,average[0]);
                average[1]=average[0];
                graph.removeAllSeries();
                graph.addSeries(new LineGraphSeries<DataPoint>(points));
                shiftColor(cpuLoad,average[0]);
                cpuLoad.setText(average[0]+"%");
                fetchRamMemoryInfo();
                System.out.println(seconds);
            }
        });
        timer.start();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ((MainActivity)getContext()).toolbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        // prevent any thread leaks and memory leaks
        stopThreads();
        if(parent!=null)
            ((MainActivity)getContext()).setFragment(parent);
    }
}
