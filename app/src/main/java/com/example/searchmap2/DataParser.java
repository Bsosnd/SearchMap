/*
* JSON파일을 파싱하는 클래스
*/

package com.example.searchmap2;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class DataParser {

    //private String step;
    private String entire_step=null;
    //private String[] getDuration;

    //출발주소, 도착주소, 총 이동시간
    private String Dur,str_Start,str_End;

    //경로와 이미지를 저장할 공간
//    ArrayList<HashMap<Integer,String>> list = new ArrayList<HashMap<Integer, String>>();
//    HashMap<Integer,String> map = new HashMap<Integer, String>();

    //private ArrayList<SampleItem> arrayList=new ArrayList<>();
    //ArrayList<SampleItem> inner_list;
    private ArrayList<ArrayList<SampleItem>> list=null;
    private ArrayList<SampleItem> inner_list = null;
    private String durArray[];

    public ArrayList<ArrayList<SampleItem>> parse(String JSON){
//        String entire_step=null;
//        String step = null;



        list  = new ArrayList<ArrayList<SampleItem>>();
        inner_list = new ArrayList<SampleItem>();


        //map = new HashMap<Integer, SampleItem>();

        int list_len;
        String step=null;

        JSONArray routesArray;
        JSONArray legsArray;
        JSONArray stepsArray;

        String[] getInstructions; //이동정보 저장
        String[] arrival_name; //대중교통 도착지 저장
        String[] depart_name; //대중교통 출발지 저장
        String[] getHeadsign;
        String[] getBusNo;//노선 정보(~호선, 버스번호)
        String[] getCurrentDur,getCurrentDis;

        //ArrayList<SampleItem> arrayList=new ArrayList<>();
        //arrayList = new ArrayList<>();
        SampleItem sampleItem;

        try{
            //arrayList.clear();
            JSONObject jsonObject = new JSONObject(JSON);
            //JSON 파일을 JSON객체로 바꿔준다.

            routesArray = jsonObject.getJSONArray("routes");
            //Object에서 routes라는 이름의 키 값을 저장

            int i=0;
            int routesSize = routesArray.length();

            do{
                //routes Array 배열의 길이만큼 반복을 돌리면서
                System.out.println("i검색  : "+ i);
                //step="<<<<<<<<"+(i+1)+"번째 경로" +">>>>>>>>>>"+"\n\n";

                System.out.println("routesArray 길이 :"+routesArray.length());
                System.out.println("routesArray"+i+" : "+routesArray.get(i));

                legsArray = ((JSONObject)routesArray.get(i)).getJSONArray("legs");
                //JSONObject legJsonObject = legsArray.getJSONObject(i);
                JSONObject legJsonObject = legsArray.getJSONObject(0);


                //출발지, 도착지(나중에는 i=0일때만 들어와서 저장할 수 있도록 하기)
                if(i==0) {
                    str_Start = legJsonObject.getString("start_address");
                    str_End = legJsonObject.getString("end_address");
                    entire_step="출발지 : "+str_Start+"\n"
                            +"도착지 : "+str_End+"\n"
                            +"-------------------------------------\n";

                }

                //총 이동시간 => 이건 leg마다 다르니까 step에 같이 출력하기
                String duration = legJsonObject.getString("duration");
                //Object에서 키 값이 duration인 변수를 찾아서 저장
                JSONObject durJsonObject = new JSONObject(duration);
                //duration에도 Object가 존재하므로 Object를 변수에 저장
                //getDuration[j] = durJsonObject.getString("text");
                Dur= durJsonObject.getString("text");
                durArray = new String[routesArray.length()];
                durArray[i]=Dur;
                //step+="총 이동시간 : "+ Dur+"\n\n";

                stepsArray = legJsonObject.getJSONArray("steps");
                list_len = stepsArray.length();

                getInstructions = new String[list_len]; //이동정보 저장
                getCurrentDur = new String[list_len];
                getCurrentDis = new String[list_len];
                arrival_name=new String[list_len]; //대중교통 도착지 저장
                depart_name=new String[list_len]; //대중교통 출발지 저장
                getHeadsign = new String[list_len];
                getBusNo = new String[list_len];//노선 정보(~호선, 버스번호)

                for(int k=0;k<list_len;k++) {
                    //확인
                    System.out.println("세번째 반복문 ");
                    System.out.println("stepsArray 길이 :" + list_len);
                    System.out.println("stepsArray" + k + "번째 : " + stepsArray.get(k));


                    JSONObject stepsObject = stepsArray.getJSONObject(k);
                    //이동정보 저장
                    getInstructions[k] = stepsObject.getString("html_instructions");

                    //각각의 이동시간(도보,대중교통)
                    String currnet_dur = stepsObject.getString("duration");
                    JSONObject CdurJsonObject = new JSONObject(currnet_dur);
                    getCurrentDur[k] = CdurJsonObject.getString("text");

                    //각각의 이동거리(도보,대중교통)
                    String currnet_dis = stepsObject.getString("distance");
                    JSONObject CdisJsonObject = new JSONObject(currnet_dis);
                    getCurrentDis[k] = CdisJsonObject.getString("text");


                    //도보인지 확인
                    String walkCheck = stepsObject.getString("travel_mode");

                    //현재 단계에서 대중교통을 이용하는지 확인
                    String[] Check = getInstructions[k].split(" ");
                    //대중교통 저장
                    String TransitCheck = Check[0];


                    //도보일 경우
                    if (walkCheck.equals("WALKING")) {
                        step += getInstructions[k] + "\n" + getCurrentDur[k] + "  |  " + getCurrentDis[k] + "이동\n";
                        //new SampleItem(step,0);
                        System.out.println("step : "+step);
                        inner_list.add(k,new SampleItem(step,0));
                        System.out.println("출력출력 : "+inner_list.get(k).getText()+" , img_num : "+inner_list.get(k).getImg_num());
                        //arrayList.add(k,new SampleItem(step,0))
                        //map.put(k,new SampleItem(step,0));

                        //System.out.println(i+"번째의 " +k+"번째의 map 출력하기 : " +map.get(k).img_num+" , "+map.get(k).getText());
                    }

                    if(TransitCheck.equals("버스")||TransitCheck.equals("Bus")
                        ||TransitCheck.equals("지하철")||TransitCheck.equals("Subway")){

                        String train_details = stepsObject.getString("transit_details");
                        JSONObject transitObject = new JSONObject(train_details);

                        String arrival_stop = transitObject.getString("arrival_stop");
                        JSONObject arrivalObject = new JSONObject(arrival_stop);
                        arrival_name[k] = arrivalObject.getString("name");

                        String depart_stop = transitObject.getString("departure_stop");
                        JSONObject departObject = new JSONObject(depart_stop);
                        depart_name[k] = departObject.getString("name");

                        getHeadsign[k] = transitObject.getString("headsign");

                        String line = transitObject.getString("line");
                        JSONObject lineObject = new JSONObject(line);
                        getBusNo[k] = lineObject.getString("short_name");


                        step += "\n" + depart_name[k] + "승차"
                                + "\n 소요시간 : " + getCurrentDur[k] + " | " + "이동거리 : " + getCurrentDis[k]
                                + "\n" + arrival_name[k] + "하차" +
                                "\n" + getHeadsign[k] + "방향"
                                +"\n 번호: " + getBusNo[k] + "\n\n";

                        if(TransitCheck.equals("버스")||TransitCheck.equals("Bus")){

                            inner_list.add(k,new SampleItem(step,1));
                            System.out.println("출력출력 : "+inner_list.get(k).getText()+" , img_num : "+inner_list.get(k).getImg_num());

                            //arrayList.add(k,new SampleItem(step,1));
                            //map.put(k,new SampleItem(step,1));
                            //System.out.println(i+"번째의 " +k+"번째의 map 출력하기 : " +map.get(k).img_num+" , "+map.get(k).getText());
                            //System.out.println("출력출력 : "+arrayList);
                        }
                        System.out.println("step : "+step);
                        if(TransitCheck.equals("지하철")||TransitCheck.equals("Subway")) {

                            inner_list.add(k,new SampleItem(step,2));
                            System.out.println("출력출력 : "+inner_list.get(k).getText()+" , img_num : "+inner_list.get(k).getImg_num());

                            //arrayList.add(k,new SampleItem(step,2));
                            //map.put(k,new SampleItem(step,2));
                            //System.out.println(i+"번째의 " +k+"번째의 map 출력하기 : " +map.get(k).img_num+" , "+map.get(k).getText());
                            //System.out.println("출력출력 : " + arrayList);
                        }
                    }

                }

                //entire_step+=step+"\n\n";
//                list.add(map);
//                map.clear();
                list.add(inner_list);
                inner_list = new ArrayList<SampleItem>();

                System.out.println("list의 "+i+"번째 : "+list.get(i));
                i++;
            }while(i<routesSize);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //list.add(map);
        //System.out.println("list출력 : "+list);
        System.out.println("entire_step : "+entire_step);
        //return entire_step;
        System.out.println("출력한다~~~~"+list);
        //System.out.println("출력한다~~~~"+arrayList.get(0));
        return list;
    }

    public String getDepartAddress(){
        return str_Start;
    }
    public String getArrivalAddress(){
        return str_End;
    }
    public String getDuration(){
        return Dur;
    }
    public String[] getDurArray(){
        return durArray;
    }
}

