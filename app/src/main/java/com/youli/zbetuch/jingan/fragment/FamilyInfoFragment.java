package com.youli.zbetuch.jingan.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.youli.zbetuch.jingan.R;
import com.youli.zbetuch.jingan.activity.PersonInfoActivity;
import com.youli.zbetuch.jingan.adapter.CommonAdapter;
import com.youli.zbetuch.jingan.entity.CommonViewHolder;
import com.youli.zbetuch.jingan.entity.EduInfo;
import com.youli.zbetuch.jingan.entity.FamilyAddressInfo;
import com.youli.zbetuch.jingan.entity.PersonInfo;
import com.youli.zbetuch.jingan.utils.MyOkHttpUtils;
import com.youli.zbetuch.jingan.view.MyListView;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

/**
 * Created by ZHengBin on 2017/8/12.
 */

public class FamilyInfoFragment extends Fragment {

    private String sfz;

//    public String getSfz() {
//        return sfz;
//    }

//    public FamilyInfoFragment(String sfz) {
//        this.sfz = sfz;
//    }

    public static final FamilyInfoFragment newInstance(String sfz){

        FamilyInfoFragment fragment = new FamilyInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putString("sfz", sfz);
        fragment.setArguments(bundle);

        return fragment;
    }

    private View contentView;

    private ListView lv;

    private List<FamilyAddressInfo> data=new ArrayList<>();
    private List<FamilyAddressInfo.FamilyAddressInfoList> hujiData=new ArrayList<>();
    private List<FamilyAddressInfo.FamilyAddressInfoList> juzhuData=new ArrayList<>();

    private CommonAdapter hujiAdapter;

    private final int SUCCESS_HUJI=10000;
    private final int SUCCESS_JUZHU=10001;
    private final int SUCCESS_PINFO=10002;
    private  final int SUCCESS_NODATA=10003;
    private final int  PROBLEM=10004;

    private Handler mHandler=new Handler(){

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){

                case SUCCESS_HUJI:

                    hujiData.addAll((List<FamilyAddressInfo.FamilyAddressInfoList>)msg.obj);
                    data.add(new FamilyAddressInfo(true, "户籍地址:", hujiData));

                    getJuzhuInfo();

                    break;

                case SUCCESS_JUZHU:

                    juzhuData.addAll((List<FamilyAddressInfo.FamilyAddressInfoList>)msg.obj);

                     data.add(new FamilyAddressInfo(false, "居住地址:", juzhuData));
                    if(getActivity()!=null) {
                        setLvAdapter(data);
                    }
                    break;

                case SUCCESS_PINFO:
                    if(getActivity()!=null) {
                        Intent intent = new Intent(getActivity(), PersonInfoActivity.class);
                        intent.putExtra("personInfos", (Serializable) ((List<PersonInfo>) msg.obj).get(0));
                        startActivity(intent);
                        getActivity().finish();
                    }
                    break;

                case PROBLEM:

                    if(getActivity()!=null) {
                        Toast.makeText(getActivity(), "网络不给力", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case SUCCESS_NODATA:
                    break;
            }

        }
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sfz=getArguments().getString("sfz");
    }

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        contentView = LayoutInflater.from(getContext()).inflate(R.layout.framgment_family_info, container, false);

          if(sfz!=null) {
              initView(contentView);
          }
        return contentView;

    }

    private void initView(View view){

        lv = (ListView) view.findViewById(R.id.lv_fmt_family_info);

        new Thread(

                new Runnable() {
                    @Override
                    public void run() {
//                        户籍地址
//                        http://web.youli.pw:89/Json/Get_family_Info.aspx?sfz=310108198004026642

//                        居住地址
//                        http://web.youli.pw:89/Json/Get_family_Info_Now.aspx?sfz=310108198004026642

                      String url= MyOkHttpUtils.BaseUrl+"/Json/Get_family_Info.aspx?sfz="+sfz;


                        Response response=MyOkHttpUtils.okHttpGet(url);

                        Message msg=Message.obtain();

                        if(response!=null){

                            try {
                                String resStr=response.body().string();

                                if(!TextUtils.equals(resStr,"")&&!TextUtils.equals(resStr,"[]")){
                                    Gson gson=new Gson();
                                    msg.obj=gson.fromJson(resStr,new TypeToken<List<FamilyAddressInfo.FamilyAddressInfoList>>(){}.getType());
                                    msg.what=SUCCESS_HUJI;
                                }else{
                                    msg.what=SUCCESS_NODATA;
                                }
                                mHandler.sendMessage(msg);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }else{

                            msg.what=PROBLEM;
                            mHandler.sendMessage(msg);

                        }

                    }
                }

        ).start();

    }

    //                        居住地址
//                        http://web.youli.pw:89/Json/Get_family_Info_Now.aspx?sfz=310108198004026642
    private void getJuzhuInfo(){

        new Thread(

                new Runnable() {
                    @Override
                    public void run() {

                        String url= MyOkHttpUtils.BaseUrl+"/Json/Get_family_Info_Now.aspx?sfz="+sfz;

                        Response response=MyOkHttpUtils.okHttpGet(url);

                        Message msg=Message.obtain();

                        if(response!=null){

                            try {
                                String resStr=response.body().string();

                                if(!TextUtils.equals(resStr,"")&&!TextUtils.equals(resStr,"[]")){
                                    Gson gson=new Gson();
                                    msg.obj=gson.fromJson(resStr,new TypeToken<List<FamilyAddressInfo.FamilyAddressInfoList>>(){}.getType());
                                    msg.what=SUCCESS_JUZHU;
                                }else{
                                    msg.what=SUCCESS_NODATA;
                                }
                                mHandler.sendMessage(msg);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }else{

                            msg.what=PROBLEM;
                            mHandler.sendMessage(msg);

                        }

                    }
                }

        ).start();

    }

    private void setLvAdapter(List<FamilyAddressInfo> list){

        if(hujiAdapter==null){

            hujiAdapter = new CommonAdapter<FamilyAddressInfo>(getActivity(), list, R.layout.item_family_info_lv) {

            @Override
            public void convert(CommonViewHolder holder, FamilyAddressInfo item, final int position) {

                ImageView iv=holder.getView(R.id.iv_item_family_info_title);

                TextView title=holder.getView(R.id.tv_item_family_info_title);
                title.setText(item.getTitle());

                MyListView childLv=holder.getView(R.id.lv_item_fmt_family_info);

                if(data.get(position).isChecked()){
                    iv.setImageResource(R.drawable.sj1);
                    childLv.setVisibility(View.VISIBLE);
                }else{
                    iv.setImageResource(R.drawable.sj);
                    childLv.setVisibility(View.GONE);
                }


                LinearLayout ll=holder.getView(R.id.ll_item_family_info_title);
                ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        if(data.get(position).isChecked()){
                            data.get(position).setChecked(false);
                        }else{
                            for(FamilyAddressInfo fai:data){
                                fai.setChecked(false);
                            }

                            data.get(position).setChecked(!data.get(position).isChecked());

                        }


                        hujiAdapter.notifyDataSetChanged();

                    }
                });

                childLv.setAdapter(new CommonAdapter<FamilyAddressInfo.FamilyAddressInfoList>(getActivity(),data.get(position).getList(),R.layout.item_item_family_info_lv) {


                    @Override
                    public void convert(CommonViewHolder holder, final FamilyAddressInfo.FamilyAddressInfoList item, int position) {

                        final ImageView head=holder.getView(R.id.iv_item_item_family_info_head);

                        new Thread(){

                            @Override
                            public void run() {

                                try {
                                    String headUrl=null;
                                    headUrl=MyOkHttpUtils.BaseUrl+"/Web/Personal/windows/ShowPic.aspx?sfz="+item.getSFZ();
                                    URL url=new URL(headUrl);

                                    InputStream is=url.openStream();
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inSampleSize = 2;//图片大小，设置越大，图片越不清晰，占用空间越小
                                     Bitmap bmp=BitmapFactory.decodeStream(is,null,options);
                                        if(bmp!=null) {

                                            final Bitmap bitmap=bmp;

                                            if(getActivity()!=null) {

                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        head.setImageBitmap(bitmap);
                                                    }
                                                });
                                            }
                                        }else{
                                            headUrl=MyOkHttpUtils.BaseUrl+"/"+item.getGetPhotoUrl();
                                            url=new URL(headUrl);
                                            is=url.openStream();
                                            bmp=BitmapFactory.decodeStream(is,null,options);
                                            final Bitmap nullBmp=bmp;
                                            if(getActivity()!=null){
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        head.setImageBitmap(nullBmp);
                                                    }
                                                });
                                            }

                                        }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        }.start();

                        TextView name=holder.getView(R.id.tv_item_item_family_info_name);
                        name.setText(item.getNAME());
                        TextView sex=holder.getView(R.id.tv_item_item_family_info_sex);
                        sex.setText(item.getSEX());
                        TextView birthday=holder.getView(R.id.tv_item_item_family_info_birthday);
                        birthday.setText(item.getBIRTH_DATE().split("T")[0]);
                        TextView idCard=holder.getView(R.id.tv_item_item_family_info_idcard);
                        idCard.setText(item.getSFZ());

                    }
                });

                childLv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                        showFamilyDialog(position);

                        return false;
                    }
                });

            }
        };


        lv.setAdapter(hujiAdapter);

        }else{
            hujiAdapter.notifyDataSetChanged();
        }

    }

    private void showFamilyDialog(final int p){
        if(getActivity()==null){
            return;
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("查看详细信息");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                getPersonInfo(p);

            }
        });

        builder.setNegativeButton("取消",null);
        builder.show();

    }

    private void  getPersonInfo(final int p){

        new Thread(

                new Runnable() {
                    @Override
                    public void run() {
                        //  http://web.youli.pw:89/Json/Get_BASIC_INFORMATION.aspx?sfz=310108198004026642
                        String url=MyOkHttpUtils.BaseUrl+"/Json/Get_BASIC_INFORMATION.aspx?sfz="+hujiData.get(p).getSFZ();

                        Response response=MyOkHttpUtils.okHttpGet(url);
                        Message msg=Message.obtain();
                        if(response!=null){

                            try {
                                String resStr=response.body().string();
                               // Log.e("2017/8/31","数据==="+resStr);

                                if(!TextUtils.equals(resStr,"")&&!TextUtils.equals(resStr,"[]")) {
                                    Gson gson = new Gson();
                                    msg.obj = gson.fromJson(resStr, new TypeToken<List<PersonInfo>>() {
                                    }.getType());

                                    msg.what = SUCCESS_PINFO;
                                }else{
                                    msg.what = SUCCESS_NODATA;
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }else{

                            msg.what=PROBLEM;

                        }

                        mHandler.sendMessage(msg);
                    }
                }

        ).start();

    }

}
