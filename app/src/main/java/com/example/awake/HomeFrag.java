package com.example.awake;

import static com.mapbox.maps.plugin.animation.CameraAnimationsUtils.getCamera;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;
import static com.mapbox.navigation.base.extensions.RouteOptionsExtensions.applyDefaultNavigationOptions;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.addOnMapClickListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.text.TextWatcher;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.Bearing;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.api.directions.v5.models.VoiceInstructions;
import com.mapbox.bindgen.Expected;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.OnMapClickListener;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentConstants;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.generated.LocationComponentSettings;
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions;
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.base.route.NavigationRoute;
import com.mapbox.navigation.base.route.NavigationRouterCallback;
import com.mapbox.navigation.base.route.RouterFailure;
import com.mapbox.navigation.base.route.RouterOrigin;
import com.mapbox.navigation.base.trip.model.RouteProgress;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.directions.session.RoutesObserver;
import com.mapbox.navigation.core.directions.session.RoutesUpdatedResult;
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter;
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp;
import com.mapbox.navigation.core.trip.session.LocationMatcherResult;
import com.mapbox.navigation.core.trip.session.LocationObserver;
import com.mapbox.navigation.core.trip.session.RouteProgressObserver;
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver;
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer;
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi;
import com.mapbox.navigation.ui.maneuver.model.Maneuver;
import com.mapbox.navigation.ui.maneuver.model.ManeuverError;
import com.mapbox.navigation.ui.maneuver.view.MapboxManeuverView;
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider;
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi;
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView;
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView;
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineError;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources;
import com.mapbox.navigation.ui.maps.route.line.model.RouteSetValue;
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi;
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer;
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement;
import com.mapbox.navigation.ui.voice.model.SpeechError;
import com.mapbox.navigation.ui.voice.model.SpeechValue;
import com.mapbox.navigation.ui.voice.model.SpeechVolume;
import com.mapbox.navigation.ui.voice.view.MapboxSoundButton;
import com.mapbox.search.autocomplete.PlaceAutocomplete;
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion;
import com.mapbox.search.ui.adapter.autocomplete.PlaceAutocompleteUiAdapter;
import com.mapbox.search.ui.view.CommonSearchViewConfiguration;
import com.mapbox.search.ui.view.SearchResultsView;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import firebase.classes.FirebaseDatabase;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

///**
// * A simple {@link Fragment} subclass.
// * Use the {@link Home_Frag#newInstance} factory method to
// * create an instance of this fragment.
// */
public class HomeFrag extends Fragment {

    private static final String TAG = "HomeFrag";
    public static ImageButton viewDetectionBtn, closeMap;
    public static ImageButton notifIcon;
    TextView timeofDay;
    public static TextView tempTV, humidTV, weatherType, weatherAddress;
    public static ImageView weatherDescIV;
    public static TextView statusDriverTV, nameDriverTV, viewAlertHistory;
    TextView tempCloud, humid, wind, rain, feelsLike, cityCountry;
    public static ConstraintLayout driverInfoLayout, weatherInfoLayout;
    public static LinearLayout driverStatusLayout;
    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDB;
    Map<String, Object> userData = new HashMap<>();
    private static final String url = "https://api.openweathermap.org/data/2.5/weather";
    private static final String appid = "449466108ec00a49fe1c77ffe6f31406";
    private static DecimalFormat df = new DecimalFormat("#.##");
    public  static boolean trueNorth=true;
    public  static boolean maneuverOn=false;
    public  static boolean muteSpeech=true;
    public static TextView wrapper;


    CameraActivity cameraActivity;



    public static MapView mapView;
    FloatingActionButton focusLocationBtn;
    ImageButton setRoute;
    private final NavigationLocationProvider navigationLocationProvider = new NavigationLocationProvider();
    private MapboxRouteLineView routeLineView;
    private MapboxRouteLineApi routeLineApi;
    private final LocationObserver locationObserver = new LocationObserver() {

        @Override
        public void onNewRawLocation(@NonNull Location location) {

        }

        @Override
        public void onNewLocationMatcherResult(@NonNull LocationMatcherResult locationMatcherResult) {
            Location location = locationMatcherResult.getEnhancedLocation();
            cameraActivity.location= location;
            cameraActivity.getCurrentAddress();
            navigationLocationProvider.changePosition(location, locationMatcherResult.getKeyPoints(), null, null);
            if (focusLocation) {
                updateCamera(Point.fromLngLat(location.getLongitude(), location.getLatitude()), (double) location.getBearing());
            }
        }
    };
    private final RoutesObserver routesObserver = new RoutesObserver() {
        @Override
        public void onRoutesChanged(@NonNull RoutesUpdatedResult routesUpdatedResult) {
            routeLineApi.setNavigationRoutes(routesUpdatedResult.getNavigationRoutes(), new MapboxNavigationConsumer<Expected<RouteLineError, RouteSetValue>>() {
                @Override
                public void accept(Expected<RouteLineError, RouteSetValue> routeLineErrorRouteSetValueExpected) {
                    Style style = mapView.getMapboxMap().getStyle();
                    if (style != null) {
                        routeLineView.renderRouteDrawData(style, routeLineErrorRouteSetValueExpected);
                    }
                }
            });
        }
    };
    boolean focusLocation = true;
    private MapboxNavigation mapboxNavigation;
    private void updateCamera(Point point, Double bearing) {

        bearing= trueNorth?0.0:bearing;
        double zoom= trueNorth?15.0:18.0;
        double pitch = maneuverOn?45.0:0.0;

        MapAnimationOptions animationOptions = new MapAnimationOptions.Builder().duration(1500L).build();
        CameraOptions cameraOptions = new CameraOptions.Builder().center(point).zoom(zoom).bearing(bearing).pitch(pitch)
                .padding(new EdgeInsets(0, 0.0, 0.0, 0.0)).build();

        getCamera(mapView).easeTo(cameraOptions, animationOptions);
    }
    private final OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
            focusLocation = false;
            getGestures(mapView).removeOnMoveListener(this);
            focusLocationBtn.show();
        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {

        }
    };
    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result) {
                Toast.makeText(CameraActivity.context, "Permission granted! Restart this app", Toast.LENGTH_SHORT).show();
            }
        }
    });

    private MapboxSpeechApi speechApi;
    private MapboxVoiceInstructionsPlayer mapboxVoiceInstructionsPlayer;

    private MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> speechCallback = new MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>>() {
        @Override
        public void accept(Expected<SpeechError, SpeechValue> speechErrorSpeechValueExpected) {
            speechErrorSpeechValueExpected.fold(new Expected.Transformer<SpeechError, Unit>() {
                @NonNull
                @Override
                public Unit invoke(@NonNull SpeechError input) {
                    if(!muteSpeech) {
                        mapboxVoiceInstructionsPlayer.play(input.getFallback(), voiceInstructionsPlayerCallback);
                    }
                    return Unit.INSTANCE;
                }
            }, new Expected.Transformer<SpeechValue, Unit>() {
                @NonNull
                @Override
                public Unit invoke(@NonNull SpeechValue input) {
                    if(!muteSpeech) {
                        mapboxVoiceInstructionsPlayer.play(input.getAnnouncement(), voiceInstructionsPlayerCallback);
                    }
                    return Unit.INSTANCE;
                }
            });
        }
    };

    private MapboxNavigationConsumer<SpeechAnnouncement> voiceInstructionsPlayerCallback = new MapboxNavigationConsumer<SpeechAnnouncement>() {
        @Override
        public void accept(SpeechAnnouncement speechAnnouncement) {
            speechApi.clean(speechAnnouncement);
        }
    };

    VoiceInstructionsObserver voiceInstructionsObserver = new VoiceInstructionsObserver() {
        @Override
        public void onNewVoiceInstructions(@NonNull VoiceInstructions voiceInstructions) {
            speechApi.generate(voiceInstructions, speechCallback);
        }
    };

    private boolean isVoiceInstructionsMuted = false;
    private PlaceAutocomplete placeAutocomplete;
    private SearchResultsView searchResultsView;
    private PlaceAutocompleteUiAdapter placeAutocompleteUiAdapter;
    private EditText searchET;
    private boolean ignoreNextQueryUpdate = false;
    private MapboxManeuverView mapboxManeuverView;
    private MapboxManeuverApi maneuverApi;
    private MapboxRouteArrowView routeArrowView;
    private MapboxRouteArrowApi routeArrowApi = new MapboxRouteArrowApi();

    private RouteProgressObserver routeProgressObserver = new RouteProgressObserver() {
        @Override
        public void onRouteProgressChanged(@NonNull RouteProgress routeProgress) {
            Style style = mapView.getMapboxMap().getStyle();
            if (style != null) {
                routeArrowView.renderManeuverUpdate(style, routeArrowApi.addUpcomingManeuverArrow(routeProgress));
            }

            maneuverApi.getManeuvers(routeProgress).fold(new Expected.Transformer<ManeuverError, Object>() {
                @NonNull
                @Override
                public Object invoke(@NonNull ManeuverError input) {
                    return new Object();
                }
            }, new Expected.Transformer<List<Maneuver>, Object>() {
                @NonNull
                @Override
                public Object invoke(@NonNull List<Maneuver> input) {
                    if(maneuverOn){
                        mapboxManeuverView.setVisibility(View.VISIBLE);
                    }
                    mapboxManeuverView.renderManeuvers(maneuverApi.getManeuvers(routeProgress));

                    return new Object();

                }
            });
        }
    };
    // Define a constant for the arrival threshold distance
    private static final double ARRIVAL_THRESHOLD_METERS = 20.0;


    public static void minimizeMap(){
        if(driverInfoLayout!=null){
            driverInfoLayout.setVisibility(View.VISIBLE);
            driverStatusLayout.setVisibility(View.VISIBLE);
            weatherInfoLayout.setVisibility(View.VISIBLE);
            closeMap.setImageResource(R.drawable.ic_expand_round_duotone_line);
        }
    }

    public static  void maximizeMap(){
        driverInfoLayout.setVisibility(View.GONE);
        driverStatusLayout.setVisibility(View.GONE);
        weatherInfoLayout.setVisibility(View.GONE);
        closeMap.setImageResource(R.drawable.ic_close_round_fill);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Log.d("Startup","Home Fragment Added");

        firebaseDB = new FirebaseDatabase();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        driverInfoLayout= view.findViewById(R.id.driverInfoLayout);
        driverStatusLayout= view.findViewById(R.id.driverStatusLayout);
        weatherInfoLayout = view.findViewById(R.id.weatherInfoLayout);
        closeMap = view.findViewById(R.id.closeMap);
        viewAlertHistory = view.findViewById(R.id.viewAlertHistory);
        wrapper = view.findViewById(R.id.wrapperTV);

        notifIcon = view.findViewById(R.id.notifIcon);
        viewDetectionBtn = view.findViewById(R.id.viewDetectionBtn);
        timeofDay = view.findViewById(R.id.timeOfDayTV);
        statusDriverTV= view.findViewById(R.id.statusDriverTV);
        nameDriverTV=view.findViewById(R.id.nameDriverTV);
        tempTV= view.findViewById(R.id.temperatureTV);
        humidTV= view.findViewById(R.id.hAndLTV);
        weatherType= view.findViewById(R.id.weatherTypeTV);
        weatherAddress = view.findViewById(R.id.weatherAddressTV);
        weatherDescIV=view.findViewById(R.id.weatherTypeIV);

        tempCloud = view.findViewById(R.id.weatherTempAndDesc);
        humid = view.findViewById(R.id.weatherTempAndDesc);
        rain = view.findViewById(R.id.weatherTempAndDesc);
        wind = view.findViewById(R.id.weatherWindSpeed);
        feelsLike = view.findViewById(R.id.weatherFeelsLike);
        cityCountry = view.findViewById(R.id.weatherCityCountry);





         cameraActivity = (CameraActivity) getActivity();
        userData= cameraActivity.userInfo;

        if(!userData.isEmpty()){
            nameDriverTV.setText(userData.get("first_name").toString() + " " + userData.get("middle_name") + ". " + userData.get("last_name") + " " + userData.get("suffix"));
        }
        statusDriverTV.setText( cameraActivity.statusDriver);
        // Set an OnClickListener for the viewDetectionBtn
        viewDetectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity.elevation= 30;
                cameraActivity.changeFrameLayoutElevation();
                CameraActivity.canAlarm=true;
            }
        });

        viewAlertHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraActivity.bottomNavIndex=4;
//                cameraActivity.bottomNavigation.show(4,true);
                cameraActivity.addFragment(new StatsFrag());
            }
        });




        timeofDay.setText("Good "+getTimeOfDay());
        maximizeMap();


        closeMap.setOnClickListener(v -> {
            if(CameraActivity.bottomNavIndex==1){

                cameraActivity.addFragment(new MenuFrag());
                minimizeMap();


            }else{
                Drawable currentDrawable = closeMap.getDrawable();
                if (currentDrawable.getConstantState().equals(getResources().getDrawable(R.drawable.ic_close_round_fill).getConstantState())) {
                    minimizeMap();
                }else{
                    maximizeMap();
                }
            }
        });

        mapView = view.findViewById(R.id.mapView);
        focusLocationBtn = view.findViewById(R.id.focusLocation);
        setRoute = view.findViewById(R.id.setRoute);
        mapboxManeuverView = view.findViewById(R.id.maneuverView);


        maneuverApi = new MapboxManeuverApi(new MapboxDistanceFormatter(new DistanceFormatterOptions.Builder(CameraActivity.context).build()));
        routeArrowView = new MapboxRouteArrowView(new RouteArrowOptions.Builder(CameraActivity.context).build());

        MapboxRouteLineOptions options = new MapboxRouteLineOptions.Builder(CameraActivity.context).withRouteLineResources(new RouteLineResources.Builder().build())
                .withRouteLineBelowLayerId(LocationComponentConstants.LOCATION_INDICATOR_LAYER).build();
        routeLineView = new MapboxRouteLineView(options);
        routeLineApi = new MapboxRouteLineApi(options);

        speechApi = new MapboxSpeechApi(CameraActivity.context, getString(R.string.mapbox_access_token), Locale.US.toLanguageTag());
        mapboxVoiceInstructionsPlayer = new MapboxVoiceInstructionsPlayer(CameraActivity.context, Locale.US.toLanguageTag());

        NavigationOptions navigationOptions = new NavigationOptions.Builder(CameraActivity.context).accessToken(getString(R.string.mapbox_access_token)).build();

        MapboxNavigationApp.setup(navigationOptions);
        if(mapboxNavigation==null){
            mapboxNavigation = new MapboxNavigation(navigationOptions);
        }else{
            System.out.println("already has");
        }

        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver);
        mapboxNavigation.registerRoutesObserver(routesObserver);
        mapboxNavigation.registerLocationObserver(locationObserver);
        mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver);

        placeAutocomplete = PlaceAutocomplete.create(getString(R.string.mapbox_access_token));
        searchET = view.findViewById(R.id.searchET);

        searchResultsView = view.findViewById(R.id.search_results_view);
        searchResultsView.initialize(new SearchResultsView.Configuration(new CommonSearchViewConfiguration()));


        placeAutocompleteUiAdapter = new PlaceAutocompleteUiAdapter(searchResultsView, placeAutocomplete, LocationEngineProvider.getBestLocationEngine(CameraActivity.context));

        final Handler mapHandler = new Handler(Looper.getMainLooper());
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(ignoreNextQueryUpdate){
                    ignoreNextQueryUpdate=false;
                }else{
                    //commented becaus of apu sessions reached to billing level

//                    placeAutocompleteUiAdapter.search(s.toString(), new Continuation<Unit>() {
//                        @androidx.annotation.NonNull
//                        @Override
//                        public CoroutineContext getContext() {
//                            return EmptyCoroutineContext.INSTANCE;
//                        }
//
//                        @Override
//                        public void resumeWith(@androidx.annotation.NonNull Object o) {
//                            mapHandler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if(!searchET.getText().toString().isEmpty()){
//                                        searchResultsView.setVisibility(View.VISIBLE);
//                                    }
//                                }
//                            });
//                        }
//                    });
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(searchET.getText().toString().isEmpty()){
                    System.out.println("trueee");
                    searchResultsView.setVisibility(View.GONE);
                }
            }
        });

        MapboxSoundButton soundButton = view.findViewById(R.id.soundButton);
        soundButton.unmute();
        soundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isVoiceInstructionsMuted = !isVoiceInstructionsMuted;
                if (isVoiceInstructionsMuted) {
                    soundButton.muteAndExtend(1500L);
                    mapboxVoiceInstructionsPlayer.volume(new SpeechVolume(0f));
                } else {
                    soundButton.unmuteAndExtend(1500L);
                    mapboxVoiceInstructionsPlayer.volume(new SpeechVolume(1f));
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(CameraActivity.context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }

        }


        if (ActivityCompat.checkSelfPermission(CameraActivity.context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(CameraActivity.context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
            activityResultLauncher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        } else {
            mapboxNavigation.startTripSession();
        }

        focusLocationBtn.hide();
        LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
        getGestures(mapView).addOnMoveListener(onMoveListener);

        setRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(cameraActivity, "Please select a location first", Toast.LENGTH_SHORT).show();

            }
        });

        mapView.getMapboxMap().loadStyleUri(Style.SATELLITE_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(20.0).build());
                locationComponentPlugin.setEnabled(true);
                locationComponentPlugin.setLocationProvider(navigationLocationProvider);
                getGestures(mapView).addOnMoveListener(onMoveListener);
                locationComponentPlugin.updateSettings(new Function1<LocationComponentSettings, Unit>() {
                    @Override
                    public Unit invoke(LocationComponentSettings locationComponentSettings) {
                        locationComponentSettings.setEnabled(true);
                        locationComponentSettings.setPulsingEnabled(true);
                        return null;
                    }
                });
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_pin);
                AnnotationPlugin annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapView);
                PointAnnotationManager pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, mapView);
                addOnMapClickListener(mapView.getMapboxMap(), new OnMapClickListener() {
                    @Override
                    public boolean onMapClick(@NonNull Point point) {
                        if(CameraActivity.elevation>0){
                            return false;
                        }
                        pointAnnotationManager.deleteAll();
                        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions().withTextAnchor(TextAnchor.CENTER).withIconImage(bitmap)
                                .withPoint(point);
                        pointAnnotationManager.create(pointAnnotationOptions);

                        trueNorth=false;
                        muteSpeech=true;
                        fetchRoute(point);


                        setRoute.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                System.out.println("set " +setRoute.getContentDescription().toString());
                                if(setRoute.getContentDescription().toString().equals("Get Directions")){
                                    setRoute.setContentDescription("Cancel");
                                    setRoute.setImageResource(R.drawable.ic_baseline_cancel_24);
                                    searchET.setVisibility(View.GONE);
                                    muteSpeech=false;
                                    maneuverOn=true;
                                    fetchRoute(point);
                                    focusLocationBtn.performClick();
                                }else  if(setRoute.getContentDescription().toString().equals("Cancel")){
                                    setRoute.setContentDescription("Get Directions");
                                    setRoute.setImageResource(R.drawable.ic_baseline_assistant_direction_24);
                                    maneuverOn=false;
                                    trueNorth=true;
                                    mapboxManeuverView.setVisibility(View.GONE);
                                    searchET.setVisibility(View.VISIBLE);
                                    maneuverApi.cancel();
//                                    mapboxNavigation.unregisterRoutesObserver(routesObserver);

                                }

                            }
                        });
                        return true;
                    }
                });
                focusLocationBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        focusLocation = true;
                        getGestures(mapView).addOnMoveListener(onMoveListener);
                        focusLocationBtn.hide();
                    }
                });

                placeAutocompleteUiAdapter.addSearchListener(new PlaceAutocompleteUiAdapter.SearchListener() {
                    @Override
                    public void onSuggestionsShown(@NonNull List<PlaceAutocompleteSuggestion> list) {

                    }

                    @Override
                    public void onSuggestionSelected(@NonNull PlaceAutocompleteSuggestion placeAutocompleteSuggestion) {
                        ignoreNextQueryUpdate = true;
                        focusLocation = false;
                        searchET.setText(placeAutocompleteSuggestion.getName());
                        searchResultsView.setVisibility(View.GONE);

                        pointAnnotationManager.deleteAll();
                        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions().withTextAnchor(TextAnchor.BOTTOM).withIconImage(bitmap)
                                .withPoint(placeAutocompleteSuggestion.getCoordinate());
                        pointAnnotationManager.create(pointAnnotationOptions);
                        updateCamera(placeAutocompleteSuggestion.getCoordinate(), 0.0);

                        setRoute.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                fetchRoute(placeAutocompleteSuggestion.getCoordinate());
                            }
                        });
                    }

                    @Override
                    public void onPopulateQueryClick(@NonNull PlaceAutocompleteSuggestion placeAutocompleteSuggestion) {
                        //queryEditText.setText(placeAutocompleteSuggestion.getName());
                    }

                    @Override
                    public void onError(@NonNull Exception e) {

                    }
                });
            }
        });

        TextView weatherMore= view.findViewById(R.id.weatherMore);

        weatherMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity.lastFragment= new HomeFrag();
                cameraActivity.addFragment(new WeatherFrag());
            }
        });

        notifIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity.lastFragment= new HomeFrag();
                cameraActivity.addFragment(new NotificationFragment());
                changeNotifIcon(false);
            }
        });



        return view;
    }

    public static void changeNotifIcon(boolean bool){
        SharedPreferences sharedPreferences = CameraActivity.context.getSharedPreferences("notification_state", Context.MODE_PRIVATE);
        boolean notificationMarked = sharedPreferences.getBoolean("notification_marked", bool);

        // Set the notification icon based on the notification state
        if (notificationMarked) {
            notifIcon.setImageResource(R.drawable.ic_bell_pin_new_notif);
        } else {
            notifIcon.setImageResource(R.drawable.ic_bell_no_notif);
        }
    }



    public static String getTimeOfDay() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return "morning";
        } else if(hour >= 12 && hour < 13) {
            return "noon";
        }else if (hour >= 13 && hour < 18) {
            return "afternoon";
        } else {
            return "evening";
        }
    }


    public static void getWeatherDetails(String city, String country) {
        String tempUrl = "";


        if(!country.equals("")){
            tempUrl = url + "?q=" + city + "," + country + "&appid=" + appid;
        }else{
            tempUrl = url + "?q=" + city + "&appid=" + appid;
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST, tempUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String output = "";
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                    JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                    String description = jsonObjectWeather.getString("description");
                    JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
                    double temp = jsonObjectMain.getDouble("temp") - 273.15;
                    double feelsLike = jsonObjectMain.getDouble("feels_like") - 273.15;
                    float pressure = jsonObjectMain.getInt("pressure");
                    int humidity = jsonObjectMain.getInt("humidity");
                    JSONObject jsonObjectWind = jsonResponse.getJSONObject("wind");
//                    JSONObject jsonObjectRain = jsonResponse.getJSONObject("rain");
                    double wind = jsonObjectWind.getDouble("speed")* 3.6;
//                    String rain = jsonObjectRain.getString("1h");
                    JSONObject jsonObjectClouds = jsonResponse.getJSONObject("clouds");
                    String clouds = jsonObjectClouds.getString("all");
                    JSONObject jsonObjectSys = jsonResponse.getJSONObject("sys");
                    String countryName = jsonObjectSys.getString("country");
                    String cityName = jsonResponse.getString("name");

                    Map<String, Object> weatherMap = new HashMap<>();
                    weatherMap.put("description", description);
                    weatherMap.put("temp", temp);
//                    CameraActivity.weatherMap.put("rain", rain);
                    weatherMap.put("feels_like", feelsLike);
                    weatherMap.put("pressure", pressure);
                    weatherMap.put("clouds", clouds);
                    weatherMap.put("humidity", humidity);
                     weatherMap.put("wind", wind);
                     weatherMap.put("clouds", clouds);
                      weatherMap.put("country", country);
                     weatherMap.put("city", cityName);

                     CameraActivity.weatherMap = new HashMap<>(weatherMap);



                    displayWeatherInfo(CameraActivity.weatherMap);

                } catch (JSONException e) {
                    System.out.println("Weather get error "+e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
               Log.e(TAG, error.toString().trim(), error);
            }


        });
        RequestQueue requestQueue = Volley.newRequestQueue(CameraActivity.context);
        requestQueue.add(stringRequest);
    }

    public static void displayWeatherInfo(Map<String, Object> weatherMap){
        if(weatherMap!=null && tempTV!=null){
            tempTV.setText(String.format("%.0f", weatherMap.get("temp"))+"°" );
            humidTV.setText("H:" + weatherMap.get("humidity") + "");
            weatherType.setText((String) weatherMap.get("description"));
            weatherAddress.setText((String) weatherMap.get("city")+", "+(String) weatherMap.get("country"));

            if(weatherMap.get("description")!=null){
                weatherDescIV.setImageDrawable(getweatherDrawable(weatherMap.get("description").toString()));
            }


        }
    }

    public static Drawable getweatherDrawable(String description){
        Drawable drawable =  ContextCompat.getDrawable(CameraActivity.context, R.drawable.weather_sunny);

        if(description.contains("scattered") || description.contains("broken")){
            if(isNightTime()){
                drawable =  ContextCompat.getDrawable(CameraActivity.context, R.drawable.weather_partly_moon);
            }else{
                drawable =  ContextCompat.getDrawable(CameraActivity.context, R.drawable.weather_partly_sunny);
            }
        }else if(description.contains("shower")){
            drawable =  ContextCompat.getDrawable(CameraActivity.context, R.drawable.weather_raining);
        }else if(description.contains("rain")){
            drawable =  ContextCompat.getDrawable(CameraActivity.context, R.drawable.weather_sun_rain);
        }else if(description.contains("thunderstorm")){
            drawable =  ContextCompat.getDrawable(CameraActivity.context, R.drawable.weather_thunderstorm);
        }
        return drawable;
    }

    private static boolean isNightTime() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour < 6 || hour > 18; // Assuming night time is from 6 PM to 6 AM
    }




    @SuppressLint("MissingPermission")
    private void fetchRoute(Point point) {
        LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(CameraActivity.context);
        locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
            @Override
            public void onSuccess(LocationEngineResult result) {
                Location location = result.getLastLocation();
                setRoute.setEnabled(false);
                setRoute.setContentDescription("Fetching route...");


                RouteOptions.Builder builder = RouteOptions.builder();
                Point origin = Point.fromLngLat(Objects.requireNonNull(location).getLongitude(), location.getLatitude());
                builder.coordinatesList(Arrays.asList(origin, point));
                builder.alternatives(false);
                builder.profile(DirectionsCriteria.PROFILE_DRIVING);
                builder.bearingsList(Arrays.asList(Bearing.builder().angle(location.getBearing()).degrees(45.0).build(), null));
                applyDefaultNavigationOptions(builder);

                mapboxNavigation.requestRoutes(builder.build(), new NavigationRouterCallback() {
                    @Override
                    public void onRoutesReady(@NonNull List<NavigationRoute> list, @NonNull RouterOrigin routerOrigin) {
                        mapboxNavigation.setNavigationRoutes(list);
                        setRoute.setEnabled(true);
                        setRoute.setContentDescription(maneuverOn?"Cancel":"Get Directions");
                    }

                    @Override
                    public void onFailure(@NonNull List<RouterFailure> list, @NonNull RouteOptions routeOptions) {
                        setRoute.setEnabled(true);
                        setRoute.setContentDescription("Set route");
                        Toast.makeText(CameraActivity.context, "Route request failed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCanceled(@NonNull RouteOptions routeOptions, @NonNull RouterOrigin routerOrigin) {

                    }
                });
            }

            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapboxNavigation.onDestroy();
        mapboxNavigation.unregisterRoutesObserver(routesObserver);
        mapboxNavigation.unregisterLocationObserver(locationObserver);
    }


}