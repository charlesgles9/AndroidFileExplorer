package com.file.manager.ui.utils;

import com.file.manager.ui.Models.AppManagerModel;
import com.file.manager.ui.Models.CustomFile;
import com.file.manager.ui.Models.RecentFileModel;

import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class Sorter {



    public Sorter(){

    }


    public static void sortRecentByDate(List<RecentFileModel> array){
        Collections.sort(array, new Comparator<RecentFileModel>() {
            @Override
            public int compare(RecentFileModel a, RecentFileModel b) {
                if(a.getLastModified()==b.getLastModified())
                    return 0;
                return a.getLastModified()<b.getLastModified()?1:-1;
            }
        });
    }
    public static void sortAppsByDate(List<AppManagerModel> array){
        Collections.sort(array, new Comparator<AppManagerModel>() {
            @Override
            public int compare(AppManagerModel a, AppManagerModel b) {
                if(a.getFile().lastModified()==b.getFile().lastModified())
                    return 0;
                return a.getFile().lastModified()<b.getFile().lastModified()?1:-1;
            }
        });
    }

    public static void sortAppsBySize(List<AppManagerModel>array){
        Collections.sort(array, new Comparator<AppManagerModel>() {
            @Override
            public int compare(AppManagerModel a, AppManagerModel b) {
                if(a.getFile().length()==b.getFile().length())
                    return 0;
                return a.getFile().length()<b.getFile().length()?1:-1;
            }
        });
    }
    public static void sortAppsAZ(List<AppManagerModel>array){
        Collections.sort(array, new Comparator<AppManagerModel>() {
            @Override
            public int compare(AppManagerModel a, AppManagerModel b) {

                return a.getName().compareToIgnoreCase(b.getName());
            }
        });
    }
    public static void sortAppsZA(List<AppManagerModel>array){
        Collections.sort(array, new Comparator<AppManagerModel>() {
            @Override
            public int compare(AppManagerModel a, AppManagerModel b) {

                return b.getName().compareToIgnoreCase(a.getName());
            }
        });
    }
    public static void sortByDate(ArrayList<CustomFile>array){
        Collections.sort(array, new Comparator<CustomFile>() {
            @Override
            public int compare(CustomFile a, CustomFile b) {
                if(a.lastModified()==b.lastModified())
                    return 0;
                return a.lastModified()<b.lastModified()?1:-1;
            }
        });
    }
    public static void sortBySize(ArrayList<CustomFile>array){
        Collections.sort(array, new Comparator<CustomFile>() {
            @Override
            public int compare(CustomFile a, CustomFile b) {
                if(a.length()==b.length())
                    return 0;
                return a.length()<b.length()?1:-1;
            }
        });
    }
    public static void sortByDateArray(File []array){
        Arrays.sort(array, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                if(a.lastModified()==b.lastModified())
                    return 0;
                return a.lastModified()<b.lastModified()?1:-1;
            }
        });
    }
    public static void AtoZ(ArrayList<CustomFile> array){
        Collections.sort(array, new Comparator<CustomFile>() {
            @Override
            public int compare(CustomFile a, CustomFile b) {
                return a.getName().compareToIgnoreCase(b.getName());
            }
        });
        sortFoldersTop(array);
    }

    public static void sortByExtension(ArrayList<CustomFile> array){
        Collections.sort(array, new Comparator<CustomFile>() {
            @Override
            public int compare(CustomFile a, CustomFile b) {
                return a.getExtension().compareToIgnoreCase(b.getExtension());
            }
        });

        sortFoldersTop(array);
    }
    public static void ZtoA(ArrayList<CustomFile>array){
        Collections.sort(array, new Comparator<CustomFile>() {
            @Override
            public int compare(CustomFile a, CustomFile b) {
                return b.getName().compareToIgnoreCase(a.getName());
            }
        });

        sortFoldersTop(array);
    }

    public static void sortZipEntry(List<FileHeader> array){
        Collections.sort(array, new Comparator<FileHeader>() {
            @Override
            public int compare(FileHeader a, FileHeader b) {

                return a.getFileName().compareToIgnoreCase(b.getFileName());
            }
        });

        ArrayList<FileHeader>folders= new ArrayList<>();
        ArrayList<FileHeader>files= new ArrayList<>();
        for(int i=0;i<array.size();i++) {
            FileHeader entry=array.get(i);
            if(entry.isDirectory()) {
                folders.add(entry);
            }else files.add(entry);

        }
        array.clear();
        array.addAll(folders);
        array.addAll(files);
    }

    public static void sortFoldersTop(ArrayList<CustomFile>array){
        ArrayList<CustomFile>folders= new ArrayList<>();
        ArrayList<CustomFile>files= new ArrayList<>();
        for(int i=0;i<array.size();i++) {
            CustomFile file=array.get(i);
            if(file.isDirectory()) {
                folders.add(file);
            }else files.add(file);

        }
        array.clear();
        array.addAll(folders);
        array.addAll(files);

    }

    public static void sortFoldersBottom(ArrayList<CustomFile>array){
        ArrayList<CustomFile>folders= new ArrayList<>();
        ArrayList<CustomFile>files= new ArrayList<>();
        for(int i=0;i<array.size();i++) {
            CustomFile file=array.get(i);
            if(file.isDirectory()) {
                folders.add(file);
            }else files.add(file);

        }
        array.clear();
        array.addAll(files);
        array.addAll(folders);
    }
}
