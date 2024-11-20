package ro.pub.cs.systems.eim.lab07.calculatorwebservice.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.util.EntityUtils;
import ro.pub.cs.systems.eim.lab07.calculatorwebservice.R;
import ro.pub.cs.systems.eim.lab07.calculatorwebservice.general.Constants;
import ro.pub.cs.systems.eim.lab07.calculatorwebservice.network.CalculatorWebServiceAsyncTask;

public class CalculatorWebServiceActivity extends AppCompatActivity {

    private EditText operator1EditText, operator2EditText;
    private TextView resultTextView;
    private Spinner operationSpinner, methodSpinner;

    private final DisplayResultButtonClickListener displayResultButtonClickListener = new DisplayResultButtonClickListener();
    private class DisplayResultButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            String operator1 = operator1EditText.getText().toString();
            String operator2 = operator2EditText.getText().toString();
            String operation = operationSpinner.getSelectedItem().toString();
            String method = String.valueOf(methodSpinner.getSelectedItemPosition());

            CalculatorWebServiceAsyncTask calculatorWebServiceAsyncTask = new CalculatorWebServiceAsyncTask(resultTextView);
            calculatorWebServiceAsyncTask.execute(operator1, operator2, operation, method);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator_web_service);

        operator1EditText = (EditText)findViewById(R.id.operator1_edit_text);
        operator2EditText = (EditText)findViewById(R.id.operator2_edit_text);
        resultTextView = (TextView)findViewById(R.id.result_text_view);
        operationSpinner = (Spinner)findViewById(R.id.operation_spinner);
        methodSpinner = (Spinner)findViewById(R.id.method_spinner);
        Button displayResultButton = (Button) findViewById(R.id.display_result_button);
        displayResultButton.setOnClickListener(displayResultButtonClickListener);

        displayResultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateResult();
            }
        });
    }

    private void calculateResult() {
        String operand1 = operator1EditText.getText().toString();
        String operand2 = operator2EditText.getText().toString();
        String operation = operationSpinner.getSelectedItem().toString();
        String method = methodSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(operand1) || TextUtils.isEmpty(operand2)) {
            resultTextView.setText("Please enter both numbers.");
            return;
        }

        // Determinăm operația corespunzătoare
        String operationCode;
        switch (operation) {
            case "plus":
                operationCode = "plus";
                break;
            case "minus":
                operationCode = "minus";
                break;
            case "times":
                operationCode = "times";
                break;
            case "divide":
                operationCode = "divide";
                break;
            default:
                resultTextView.setText("Invalid operation.");
                return;
        }

        if (method.equals("GET")) {
            performGetRequest(operationCode, operand1, operand2);
        } else if (method.equals("POST")) {
            performPostRequest(operationCode, operand1, operand2);
        } else {
            resultTextView.setText("Invalid method.");
        }
    }

    private void performGetRequest(String operation, String t1, String t2) {
        new Thread(() -> {
            String url = Constants.GET_WEB_SERVICE_ADDRESS + "?operation=" + operation + "&t1=" + t1 + "&t2=" + t2;

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet httpGet = new HttpGet(url);
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    String responseBody = EntityUtils.toString(entity);
                    runOnUiThread(() -> resultTextView.setText(responseBody));
                } else {
                    runOnUiThread(() -> resultTextView.setText("No response from server."));
                }
            } catch (ClientProtocolException e) {
                runOnUiThread(() -> resultTextView.setText("Client Protocol Error: " + e.getMessage()));
            } catch (IOException e) {
                runOnUiThread(() -> resultTextView.setText("IO Error: " + e.getMessage()));
            }
        }).start();
    }

    private void performPostRequest(String operation, String t1, String t2) {
        new Thread(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(Constants.POST_WEB_SERVICE_ADDRESS);

                // Create JSON body
                String jsonBody = "{ \"operation\": \"" + operation + "\", \"t1\": \"" + t1 + "\", \"t2\": \"" + t2 + "\" }";
                StringEntity entity = new StringEntity(jsonBody);

                // Set headers
                httpPost.setEntity(entity);
                httpPost.setHeader("Content-Type", "application/json");

                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity responseEntity = response.getEntity();

                if (responseEntity != null) {
                    String responseBody = EntityUtils.toString(responseEntity);
                    runOnUiThread(() -> resultTextView.setText(responseBody));
                } else {
                    runOnUiThread(() -> resultTextView.setText("No response from server."));
                }
            } catch (ClientProtocolException e) {
                runOnUiThread(() -> resultTextView.setText("Client Protocol Error: " + e.getMessage()));
            } catch (IOException e) {
                runOnUiThread(() -> resultTextView.setText("IO Error: " + e.getMessage()));
            }
        }).start();
    }
}
