//package fans.skin.expresstrade.managers.network;
//
//import android.annotation.*;
//import android.os.*;
//import android.webkit.*;
//
//import fans.skin.expresstrade.*;
//import fans.skin.expresstrade.managers.pref.*;
//
//import org.json.*;
//
//import static fans.skin.expresstrade.managers.event.EventManager.*;
//import static fans.skin.expresstrade.managers.notify.NotifyManager.*;
//
//@SuppressLint("SetJavaScriptEnabled")
//public class NetworkSocket {
//    // =============================================================================================
//    // VARIABLES
//    // =============================================================================================
//
//    private SockJSInstance sInstance;
//    private Handler handler = new Handler();
//
//    public final static int SOCKET_SWINE_NEW_NEARBY = 10; // 5
//    public final static int SOCKET_SWINE_NEW_UPDATES = 11; // 10
//    // ***
//    public final static int SOCKET_LIKED_SWINE = 20;
//    // ***
//    public final static int SOCKET_FRIEND_NEW_REQUEST = 40; // 6
//    public final static int SOCKET_FRIEND_REQUEST_IS_ACCEPTED = 41; // 7
//    // ***
//    public final static int SOCKET_EVENT_NEW = 50; // 30
//
//    public boolean isConnectActive = false;
//
//    // =============================================================================================
//    // CONSTRUCTOR
//    // =============================================================================================
//
//    public NetworkSocket() {
//        initial();
//    }
//
//    // =============================================================================================
//    // GENERAL METHODS
//    // =============================================================================================
//
//    public void initial() {
//        if (sInstance != null) return;
//
//        handler.post(new Runnable() {
//            public void run() {
//                sInstance = new SockJSInstance();
//            }
//        });
//    }
//
//    public void reconnect() {
//        //  Toast.makeText(App.context, "CONNECT", Toast.LENGTH_SHORT).show();
//
//        handler.post(new Runnable() {
//            public void run() {
//                isConnectActive = false;
//                if (sInstance == null) return;
//
//                sInstance.mWebView.loadUrl("javascript:socket = new SockJS(\"" + App.URL_SOCKET + "\"); socket.onopen = function() { Android.onopen() }; socket.onclose = function() { Android.onclose() }; socket.onmessage = function(e) { Android.onmessage(e.data) };");
//            }
//        });
//    }
//
//    public void send(final String msg) {
//        //   Toast.makeText(App.context, "SEND " + msg, Toast.LENGTH_SHORT).show();
//
//        handler.post(new Runnable() {
//            public void run() {
//                if (sInstance == null) return;
//
//                // Формируем сообщение
//                JSONArray array = new JSONArray();
//                array.put(msg);
//                String message = array.toString();
//                message = message.substring(1, message.length() - 1);
//
//                sInstance.mWebView.loadUrl("javascript:socket.send(" + message + ");");
//            }
//        });
//    }
//
//    public void close() {
//        //   Toast.makeText(App.context, "CLOSE", Toast.LENGTH_SHORT).show();
//
//        handler.post(new Runnable() {
//            public void run() {
//                isConnectActive = false;
//
//                if (sInstance == null) return;
//
//                // Destroy
//                sInstance.mWebView.destroy();
//                sInstance = null;
//            }
//        });
//    }
//
//    // ***
//
//    private void authorize() {
//        if (!App.accountModule.isAuthorized || !isConnectActive) return;
//
//        //   Toast.makeText(App.context, "AUTHORIZE", Toast.LENGTH_SHORT).show();
//
////        String token = App.connectModule.getSocketToken();
////        if (token != null) {
////            App.logManager.debug("Отправляем токен " + token);
////            send(token);
////        } else {
////            App.logManager.debug("Оправляем запрос на получение токена");
////            handler.postDelayed(new Runnable() {
////                @Override
////                public void run() {
////                    App.connectModule.updateSocketToken(false);
////                    authorize();
////                }
////            }, 5000);
////        }
//    }
//
//    // =============================================================================================
//    // CLASS SockJSInstance
//    // =============================================================================================
//
//    private class SockJSInstance {
//        private WebView mWebView;
//
//        @SuppressLint("AddJavascriptInterface")
//        public SockJSInstance() {
//
//            // Создаем WebView
//            mWebView = new WebView(App.context);
//            mWebView.getSettings().setJavaScriptEnabled(true);
//
//            // Создаем интерфейс для общения
//            mWebView.addJavascriptInterface(new SockJSJavascriptInterface(), "Android");
//
//            // Создаем строку, содержащую html
//            String string = "<html><head>" +
//                    "<script src=\"file:///android_asset/js/sockjs.min.js\"></script>" +
//                    "<script>" +
//                    "socket = new SockJS(\"" + App.URL_SOCKET + "\");" +
//                    "socket.onopen = function() { Android.onopen() };" +
//                    "socket.onclose = function() { Android.onclose() };" +
//                    "socket.onmessage = function(e) { Android.onmessage(e.data) };" +
//                    "</script></head><body></body></html>";
//
//            // Получаем html
//            mWebView.loadDataWithBaseURL("file:///android_asset/js/", string, "text/html", "UTF-8", null);
//        }
//    }
//
//    // =============================================================================================
//    // CLASS SockJSJavascriptInterface
//    // =============================================================================================
//
//    private class SockJSJavascriptInterface {
//        public SockJSJavascriptInterface() {
//        }
//
//        @JavascriptInterface
//        public void onopen() {
//            //      Toast.makeText(App.context, "ON OPEN", Toast.LENGTH_SHORT).show();
//
//            handler.post(new Runnable() {
//                public void run() {
//                    isConnectActive = true;
//
//                    App.logManager.debug("ON OPEN");
//
//                    if (!App.accountModule.isAuthorized) return;
//
//                    // Авторизовываем
//                    authorize();
//                }
//            });
//        }
//
//        @JavascriptInterface
//        public void onmessage(final String message) {
//            //     Toast.makeText(App.context, "ON MESSAGE " + message, Toast.LENGTH_SHORT).show();
//
//            handler.post(new Runnable() {
//                public void run() {
//                    String parse = message.substring(1, message.length() - 1);
//
//                    if (String.valueOf(parse.charAt(0)).equals("-")) {
//                        // error
//                        if (String.valueOf(parse.charAt(1)).equals("2")) {
//                            App.connectModule.updateSocketToken(true);
//                            reconnect();
//                            App.logManager.debug("ON MESSAGE " + parse);
//                        }
//                    } else {
//                        // response
//                        App.logManager.debug("ON MESSAGE " + parse);
//                        String[] array = parse.split(",");
//
//                        if (array.length == 0) return;
//
//                        switch (Integer.parseInt(array[0])) {
//
//                            // Новый свайн поблизости
//                            case SOCKET_SWINE_NEW_NEARBY:
//                                App.swinesModule.doSocket(NotifyType.SWINE_NEARBY, array);
//                                break;
//
//                            // Новый свайн от подписок
//                            case SOCKET_SWINE_NEW_UPDATES:
//                                App.swinesModule.doSocket(NotifyType.SWINE_UPDATES, array);
//                                break;
//
//                            // Новый лайк
//                            case SOCKET_LIKED_SWINE:
//                                App.swinesModule.doSocket(array);
//                                App.notifyModule.doSocket(array);
//                                App.eventManager.doEvent(EventType.ON_SOCKET_SWINE_LIKED, array);
//                                break;
//
//                            // Новый запрос (подписка)
//                            case SOCKET_FRIEND_NEW_REQUEST:
//                                App.usersModule.doSocketFriend(array);
//                                App.notifyModule.doSocket(array);
//                                App.eventManager.doEvent(EventType.ON_SOCKET_FRIEND_REQUEST_NEW, array);
//                                break;
//
//                            // Запрос (взаимная подписка)
//                            case SOCKET_FRIEND_REQUEST_IS_ACCEPTED:
//                                App.usersModule.doSocketFriend(array);
//                                App.notifyModule.doSocket(array);
//                                App.eventManager.doEvent(EventType.ON_SOCKET_FRIEND_REQUEST_IS_ACCEPTED, array);
//                                break;
//
//                            // Событие от сервера
//                            case SOCKET_EVENT_NEW:
//                                App.serviceModule.doSocketEvent(array);
//                                App.eventManager.doEvent(EventType.ON_EVENT_NEW, array);
//                                break;
//                        }
//
//                        App.logManager.debug("PARSE " + array[0]);
//                    }
//                }
//            });
//        }
//
//        @JavascriptInterface
//        public void onclose() {
//            //       Toast.makeText(App.context, "ON CLOSE", Toast.LENGTH_SHORT).show();
//
//            isConnectActive = false;
//            if (!App.accountModule.isAuthorized) return;
//
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    reconnect();
//                }
//            }, 5000);
//
//            App.logManager.debug("ON CLOSE");
//        }
//    }
//}
