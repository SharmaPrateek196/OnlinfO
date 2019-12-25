package com.example.onlinfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PaytmFragment extends Fragment {

    private Button btn_donate;
    private EditText et_amount;
    private View v;
    private ProgressDialog loading_dialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_paytm,null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loading_dialog=new ProgressDialog(getActivity());
        loading_dialog.setCancelable(false);
        loading_dialog.setMessage("Accessing Paytm Payment Gateway...");
        loading_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        v=view;
        //paytm permissions
        if (checkPermission()) {//not already granted
           askPermission();
        }
        else
        {
            permissionGranted(view);//already granted
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        loading_dialog.dismiss();
    }

    private void permissionGranted(View view) {
        btn_donate=(Button)view.findViewById(R.id.btn_donate);
        et_amount=(EditText)view.findViewById(R.id.et_amount);

        final String mid="auGeBx90119977819521";
        final String customer_id= FirebaseAuth.getInstance().getUid();
        final String order_id= UUID.randomUUID().toString().substring(0,28);

        btn_donate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(check())
                {
                    if(checkPermission()){//permission not granted
                        askPermission();
                    }
                    loading_dialog.show();
                    //url is the link of the website 000webhost where we have placed our paytm folder in php
                    String url="https://sharmaprateek.000webhostapp.com/paytm/paytm/generateChecksum.php";
                    final String callbackUrl="https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp";

                    RequestQueue requestQueue= Volley.newRequestQueue(getActivity());

                    StringRequest stringRequest=new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject=new JSONObject(response);
                                if(jsonObject.has("CHECKSUMHASH"))
                                {
                                    String CHECKSUMHASH=jsonObject.getString("CHECKSUMHASH");
                                    PaytmPGService paytmPGService=PaytmPGService.getStagingService();
                                    HashMap<String, String> paramMap = new HashMap<String,String>();
                                    paramMap.put( "MID" , mid);
                                    paramMap.put( "ORDER_ID" , order_id);
                                    paramMap.put( "CUST_ID" , customer_id);
                                    paramMap.put( "CHANNEL_ID" , "WAP");
                                    paramMap.put( "TXN_AMOUNT" ,et_amount.getText().toString());
                                    paramMap.put( "WEBSITE" , "WEBSTAGING");
                                    paramMap.put( "INDUSTRY_TYPE_ID" , "Retail");
                                    paramMap.put( "CALLBACK_URL", callbackUrl);
                                    paramMap.put("CHECKSUMHASH",CHECKSUMHASH);

                                    PaytmOrder order=new PaytmOrder(paramMap);

                                    paytmPGService.initialize(order,null);
                                    paytmPGService.startPaymentTransaction(getActivity(), true, true, new PaytmPaymentTransactionCallback() {
                                        @Override
                                        public void onTransactionResponse(Bundle inResponse) {
                                            loading_dialog.dismiss();
                                            Toast.makeText(getActivity(), "Paytm transaction response : "+inResponse.toString(), Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void networkNotAvailable() {
                                            loading_dialog.dismiss();
                                            Toast.makeText(getActivity(), "Network Connection Error : Check internet connectivity", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void clientAuthenticationFailed(String inErrorMessage) {
                                            loading_dialog.dismiss();
                                            Toast.makeText(getActivity(), "Client Authentication Failed: Server Error "+ inErrorMessage.toString(), Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void someUIErrorOccurred(String inErrorMessage) {
                                            loading_dialog.dismiss();
                                            Toast.makeText(getActivity(), "UI Error : "+ inErrorMessage, Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                                            loading_dialog.dismiss();
                                            Toast.makeText(getActivity(), "Unable to load webpage "+ inErrorMessage.toString(), Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onBackPressedCancelTransaction() {
                                            loading_dialog.dismiss();
                                            Toast.makeText(getActivity(), "Transaction Cancelled", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                                            loading_dialog.dismiss();
                                            Toast.makeText(getActivity(), "Transaction Cancelled"+ inResponse.toString(), Toast.LENGTH_SHORT).show();

                                        }
                                    });
                                }
                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            loading_dialog.dismiss();
                            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> paramMap = new HashMap<String,String>();
                            paramMap.put( "MID" , mid);
// Key in your staging and production MID available in your dashboard
                            paramMap.put( "ORDER_ID" , order_id);
                            paramMap.put( "CUST_ID" , customer_id);
                            paramMap.put( "CHANNEL_ID" , "WAP");
                            paramMap.put( "TXN_AMOUNT" ,et_amount.getText().toString());
                            paramMap.put( "WEBSITE" , "WEBSTAGING");
                            paramMap.put( "INDUSTRY_TYPE_ID" , "Retail");
                            paramMap.put( "CALLBACK_URL", callbackUrl);
                            return paramMap;
                        }
                    };
                    requestQueue.add(stringRequest);
                }
            }
        });

    }

    private void askPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
        {return true;}//not granted
        return false; //granted
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {//permission granted
            permissionGranted(v);
        }
        else
        {
            askPermission();
        }
    }

    private boolean check() {
        if(et_amount.getText().toString().equals(""))
        {
            Toast.makeText(getActivity(), "Please enter amount", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

}
