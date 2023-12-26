package com.example.conectamovil;
import com.example.conectamovil.LatLngWrapper;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class mapa extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private DatabaseReference databaseReference;
    private Polyline rutaPolyline;
    private PlacesClient placesClient;
    private Location lastKnownLocation;
    private Marker currentLocationMarker;
    private String distanciaTotal = "";
    private String duracionTotal = "";

    private EditText editTextDestination;
    private Button btnNavigate;
    private Button btnToggleMapType;
    private Button btnAddMarker;

    private int currentMapType = GoogleMap.MAP_TYPE_NORMAL;  // Inicialmente, el tipo de mapa es normal

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disemapa);

        // Configura Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("ubicaciones").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Verifica y solicita permisos de ubicación si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

        // Inicializa Places API
        Places.initialize(getApplicationContext(), "AIzaSyB00aT_tspvpcjYPDUnPTzYNXefFz3l7CY");
        placesClient = Places.createClient(this);

        // Inicializa el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(this);

        // Inicializa el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Configura la actualización de la ubicación en tiempo real
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    lastKnownLocation = location;

                    if (rutaPolyline == null) {
                        rutaPolyline = mMap.addPolyline(new PolylineOptions().width(5).color(Color.BLUE));
                    }

                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));

                    guardarUbicacionEnFirebase(location.getLatitude(), location.getLongitude());

                    if (mMap != null) {
                        if (currentLocationMarker != null) {
                            currentLocationMarker.remove();
                        }

                        currentLocationMarker = mMap.addMarker(new MarkerOptions()
                                .position(currentLatLng)
                                .title("Ubicación Actual")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    }

                    if (rutaPolyline != null) {
                        List<LatLng> puntosRuta = rutaPolyline.getPoints();
                        puntosRuta.add(currentLatLng);
                        rutaPolyline.setPoints(puntosRuta);
                    }
                }
            }
        };

        // Referencias a las vistas
        editTextDestination = findViewById(R.id.editTextOrigin);
        btnNavigate = findViewById(R.id.btnNavigate);
        btnToggleMapType = findViewById(R.id.btnToggleMapType);
        btnAddMarker = findViewById(R.id.btnAddMarker);

        // Configura el botón de navegación
        btnNavigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String destinationAddress = editTextDestination.getText().toString();
                if (!destinationAddress.isEmpty()) {
                    marcarDireccion(destinationAddress);
                } else {
                    Toast.makeText(mapa.this, "Ingrese la dirección de destino", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Configura el botón para cambiar el tipo de mapa
        btnToggleMapType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMapType();
            }
        });

        // Configura el botón para añadir marcadores
        btnAddMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lastKnownLocation != null) {
                    LatLng currentLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Marcador Significativo"));
                } else {
                    Toast.makeText(mapa.this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void toggleMapType() {
        if (mMap != null) {
            // Alterna entre normal y satelital
            currentMapType = (currentMapType == GoogleMap.MAP_TYPE_NORMAL) ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL;
            mMap.setMapType(currentMapType);
        }
    }

    private void marcarDireccion(String address) {
        try {
            Geocoder geocoder = new Geocoder(this);
            List<Address> addresses = geocoder.getFromLocationName(address, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address destinationAddress = addresses.get(0);
                LatLng destinationLatLng = new LatLng(destinationAddress.getLatitude(), destinationAddress.getLongitude());

                mMap.clear();

                // Agrega marcadores en la dirección de inicio y fin
                mMap.addMarker(new MarkerOptions().position(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude())).title("Inicio"));
                mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Fin"));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 15f));

                // Inicializa listaPuntos antes de utilizarla
                List<LatLng> listaPuntos = new ArrayList<>();

                if (lastKnownLocation != null) {
                    String url = obtenerUrlDirecciones(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), destinationLatLng);
                    new ObtenerDireccionesAsyncTask(listaPuntos).execute(url);
                } else {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 15f));
                    Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show();
                }

                // Guarda la ruta en Firebase
                guardarRutaEnFirebase(listaPuntos, destinationAddress.getAddressLine(0), address, distanciaTotal, duracionTotal);

            } else {
                Toast.makeText(this, "No se encontró la dirección", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Guardar ruta en Firebase
    private String obtenerDireccionActual(double latitud, double longitud) {
        String direccionActual = "Dirección no disponible";

        try {
            Geocoder geocoder = new Geocoder(this);
            List<Address> addresses = geocoder.getFromLocation(latitud, longitud, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                direccionActual = address.getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return direccionActual;
    }

    private void guardarRutaEnFirebase(List<LatLng> listaPuntos, String direccionInicio, String direccionFin, String distancia, String duracion) {
        List<LatLngWrapper> listaPuntosWrapper = new ArrayList<>();
        for (LatLng punto : listaPuntos) {
            listaPuntosWrapper.add(new LatLngWrapper(punto.latitude, punto.longitude));
        }

        Ruta ruta = new Ruta();
        ruta.setCoordenadas(listaPuntosWrapper);
        ruta.setDireccionInicio(direccionInicio);
        ruta.setDireccionFin(direccionFin);
        ruta.setDistancia(distancia);
        ruta.setDuracion(duracion);

        DatabaseReference rutaReference = databaseReference.child("rutas").push();
        rutaReference.setValue(ruta);
    }






    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null && mMap != null) {
                            LatLng lastKnownLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng, 15f));
                        } else {
                            Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show();
                        }
                    });

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fusedLocationClient != null) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    // Guardar ubicación en Firebase
    private void guardarUbicacionEnFirebase(double latitud, double longitud) {
        DatabaseReference ubicacionReference = databaseReference.child("ubicaciones").push();

        Map<String, Object> ubicacionMap = new HashMap<>();
        ubicacionMap.put("latitud", latitud);
        ubicacionMap.put("longitud", longitud);
        ubicacionMap.put("hora", System.currentTimeMillis()); // Agrega la marca de tiempo

        ubicacionReference.setValue(ubicacionMap);
    }

    private String obtenerUrlDirecciones(LatLng origin, LatLng destination) {
        String strOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        String strDestination = "destination=" + destination.latitude + "," + destination.longitude;
        String mode = "mode=driving";
        String parameters = strOrigin + "&" + strDestination + "&" + mode;
        String output = "json";
        String apiKey = "AIzaSyB00aT_tspvpcjYPDUnPTzYNXefFz3l7CY";

        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + apiKey;
    }

    private class ObtenerDireccionesAsyncTask extends AsyncTask<Object, Void, String> {
        private List<LatLng> listaPuntos;

        public ObtenerDireccionesAsyncTask(List<LatLng> listaPuntos) {
            this.listaPuntos = listaPuntos;
        }

        @Override
        protected String doInBackground(Object... params) {
            String url = (String) params[0];
            return Utilidades.obtenerDatosDesdeUrl(url);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                trazarRuta(result);
            }
        }
    }

    private void trazarRuta(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray routes = jsonObject.getJSONArray("routes");

            JSONObject route = routes.getJSONObject(0);
            JSONArray legs = route.getJSONArray("legs");

            List<LatLng> listaPuntos = new ArrayList<>();

            // Variables para almacenar la distancia y duración total
            String distanciaTotal = "";
            String duracionTotal = "";

            for (int j = 0; j < legs.length(); j++) {
                JSONObject leg = legs.getJSONObject(j);

                // Obtiene la distancia total
                JSONObject distance = leg.getJSONObject("distance");
                distanciaTotal = distance.getString("text");

                // Obtiene la duración total
                JSONObject duration = leg.getJSONObject("duration");
                duracionTotal = duration.getString("text");

                JSONArray steps = leg.getJSONArray("steps");

                for (int k = 0; k < steps.length(); k++) {
                    JSONObject step = steps.getJSONObject(k);
                    JSONObject polyline = step.getJSONObject("polyline");
                    String points = polyline.getString("points");
                    List<LatLng> puntosRuta = PolyUtil.decode(points);

                    listaPuntos.addAll(puntosRuta);
                }
            }

            if (rutaPolyline != null) {
                rutaPolyline.remove();
            }

            // Actualiza la información de distancia y duración en la interfaz de usuario
            mostrarInformacionRuta(distanciaTotal, duracionTotal);

            // Dibuja la ruta en el mapa
            rutaPolyline = mMap.addPolyline(new PolylineOptions()
                    .addAll(listaPuntos)
                    .width(5)
                    .color(Color.RED));

            // Guarda la ruta en Firebase con distancia y duración
            guardarRutaEnFirebase(listaPuntos, "Inicio", "Fin", distanciaTotal, duracionTotal);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private void mostrarInformacionRuta(String distancia, String duracion) {
        // Actualiza la interfaz de usuario con la distancia y duración
        Toast.makeText(this, "Distancia: " + distancia + "\nDuración: " + duracion, Toast.LENGTH_LONG).show();
    }


}
