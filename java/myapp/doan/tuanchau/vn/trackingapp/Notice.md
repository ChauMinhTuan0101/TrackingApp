//    private class GetAllCar extends AsyncTask<Void, Void, Void >{
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            HttpHandler sh = new HttpHandler();
//            String jsonStr =sh.makeServiceCall("http://trackingcar.us-west-2.elasticbeanstalk.com/api/getAllCar");
//            Log.e("Server Say","Response From server: "+ jsonStr);
//            if (jsonStr != null) {
//                try {
//                    JSONObject jsonObj = new JSONObject(jsonStr);
//
//                    JSONArray contacts = jsonObj.getJSONArray("data");
//
//                    // looping through All Contacts
//                    for (int i = 0; i < contacts.length(); i++) {
//                        JSONObject c = contacts.getJSONObject(i);
//
//                        String imei = c.getString("IMEI");
//                        String status = c.getString("Status");
//                        String date = c.getString("CreatedDate");
//                        String name = c.getString("Name");
//
//
//                        // Toast.makeText(MainActivity.this, imei+" "+name, Toast.LENGTH_SHORT).show();
//
//                        // adding each child node to HashMap key => value
//                        eTracking et = new eTracking(name,imei,date);
//                        trackingList.add(et);
//
//                        // adding contact to contact list
//                        //contactList.add(contact);
//                    }
//                } catch (final JSONException e) {
//                    Log.e("Server Said:", "Json parsing error: " + e.getMessage());
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(getApplicationContext(),
//                                    "Json parsing error: " + e.getMessage(),
//                                    Toast.LENGTH_LONG)
//                                    .show();
//                        }
//                    });
//
//                }
//            } else {
//                Log.e("Server Said: ", "Couldn't get json from server.");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(),
//                                "Couldn't get json from server. Check LogCat for possible errors!",
//                                Toast.LENGTH_LONG)
//                                .show();
//                    }
//                });
//
//            }
//
//            return null;
//        }
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            // Showing progress dialog
//            pDialog = new ProgressDialog(MainActivity.this);
//            pDialog.setMessage("Please wait...");
//            pDialog.setCancelable(false);
//            pDialog.show();
//
//        }
//        @Override
//        protected void onPostExecute(Void result) {
//            super.onPostExecute(result);
//            // Dismiss the progress dialog
//            if (pDialog.isShowing())
//                pDialog.dismiss();
//            /**
//             * Updating parsed JSON data into ListView
//             * */
//            for(eTracking item: trackingList)
//            {
//                Toast.makeText(MainActivity.this, item.getName() + " " + item.getImei(), Toast.LENGTH_SHORT).show();
//            }
//        }
//
//    }