package com.ivoriechat.android.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.reactivex.rxjava3.observers.DefaultObserver;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import com.ivoriechat.android.R;
import com.ivoriechat.android.authentication.AuthenUtils;
import com.ivoriechat.android.utils.AppGeneral;
import com.ivoriechat.android.utils.ProxyVideoSink;
import com.ivoriechat.android.utils.WebsocketService;
import com.ivoriechat.android.utils.CustomPeerConnectionObserver;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoChatFragment extends Fragment {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final int REQUEST_PERMISSION_CAMERA_AND_AUDIO = 0;

    private static final String TAG = "VideoChatFragment";
    private WebsocketService mWebsocketService;
    //The boolean variable to indicate whether the service is connected or not
    private boolean serviceBound = false;
    private WebsocketServiceConnection mServiceConnection;
    private BehaviorSubject<String> websocketServiceResult;
    private Integer mMentorUserId = null;
    private Integer mTargetUserId = null;
    private Boolean mBeingMentor = false;

    // Views
    private ConstraintLayout mConstraintLayout;

    // WebRTC Constants
    private static final String AUDIO_CODEC_PARAM_BITRATE = "maxaveragebitrate";
    private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
    private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
    private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";
    // WebRTC variables
    private MediaConstraints audioConstraints;
    private MediaConstraints sdpMediaConstraints;
    private PeerConnectionFactory peerConnectionFactory;
    private MediaStream mediaStream;
    private SurfaceTextureHelper surfaceTextureHelper;
    private EglBase rootEglBase = EglBase.create();
    private ProxyVideoSink remoteProxyVideoSink;
    private ProxyVideoSink localProxyVideoSink;
    // Queued remote ICE candidates are consumed only after both local and
    // remote descriptions are set. Similarly local ICE candidates are sent to
    // remote peer after both local and remote description are set.
    private List<IceCandidate> queuedRemoteCandidates = new ArrayList<>();
    private List<IceCandidate> queuedLocalCandidates = new ArrayList<>();
    private List<PeerConnection.IceServer> iceServers = new ArrayList<>();

    private PeerConnection localPeerConnection;
    private SessionDescription localSdp = null; // either offer or answer SDP
    private SessionDescription remoteSdp = null; // either offer or answer SDP
    private MySdpObserver sdpObserver = new MySdpObserver();

    private Boolean isInitiator = false;
    private MediaStream remoteMediaStream = null;


    public VideoChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        if (args != null) {
            mBeingMentor = args.getBoolean(AppGeneral.BEING_MENTOR);
            String mentorIdString = args.getString(AppGeneral.MENTOR_ID);
            if (mentorIdString != null) {
                mMentorUserId = Integer.parseInt(mentorIdString);
            }
        }

        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            // Already have permission, do the thing
            // continue execute
            // Bind WebsocketService
            Intent mServiceIntent = new Intent(getActivity(), WebsocketService.class);
            mServiceConnection = new WebsocketServiceConnection();
            getActivity().bindService(mServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.request_for_camera_and_audio_permissions), REQUEST_PERMISSION_CAMERA_AND_AUDIO, perms);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_video_chat, container, false);
        mConstraintLayout = rootView.findViewById(R.id.mConstraintLayout);

        //Initialize PeerConnectionFactory globals.
        //Params are context, initAudio,initVideo and videoCodecHwAcceleration
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(getActivity())
                        .setEnableInternalTracer(true)
                        .createInitializationOptions());

        //Create a new PeerConnectionFactory instance.
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();

        EglBase.Context eglBaseContext = EglBase.create().getEglBaseContext();

        DefaultVideoEncoderFactory defaultVideoEncoderFactory = new DefaultVideoEncoderFactory(
                eglBaseContext,  /* enableIntelVp8Encoder */true,  /* enableH264HighProfile */true);
        DefaultVideoDecoderFactory defaultVideoDecoderFactory = new DefaultVideoDecoderFactory(eglBaseContext);

        peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(defaultVideoEncoderFactory)
                .setVideoDecoderFactory(defaultVideoDecoderFactory)
                .setOptions(options)
                .createPeerConnectionFactory();

        // Create audio constraints and SDP constraints.
        createMediaConstraintsInternal();


        String mediaStreamName = "Event Live Stream";
        mediaStream = peerConnectionFactory.createLocalMediaStream(mediaStreamName);
        mediaStream.addTrack(createAudioTrack());
        //Now create a VideoCapturer instance. Callback methods are there if you want to do something! Duh!
        VideoCapturer videoCapturer = createVideoCapturer();
        VideoTrack localVideoTrack = createVideoTrack(videoCapturer);
        mediaStream.addTrack(localVideoTrack);

        //create surface renderer, init it and add the renderer to the track
        SurfaceViewRenderer localVideoView = rootView.findViewById(R.id.local_video_renderer);
        localVideoView.setMirror(true);
        localVideoView.init(rootEglBase.getEglBaseContext(), null);
        localProxyVideoSink = new ProxyVideoSink();
        localProxyVideoSink.setTarget(localVideoView);
        localVideoTrack.addSink(localProxyVideoSink);

        SurfaceViewRenderer remoteVideoView = rootView.findViewById(R.id.remote_video_renderer);
        remoteVideoView.setMirror(true);
        remoteVideoView.init(rootEglBase.getEglBaseContext(), null);
        remoteProxyVideoSink = new ProxyVideoSink();
        remoteProxyVideoSink.setTarget(remoteVideoView);

        return rootView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_CAMERA_AND_AUDIO)
    private void permissionGranted() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
            // Already have permission, do the thing
        } else {
            // Do not have permissions, request them now
            getActivity().finish();
        }
    }

    private AudioTrack createAudioTrack() {
        //create an AudioSource instance
        AudioSource audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
        String audioTrackId = "Event Live Audio Track";
        AudioTrack localAudioTrack = peerConnectionFactory.createAudioTrack(audioTrackId, audioSource);
        localAudioTrack.setEnabled(true);
        return localAudioTrack;
    }

    private VideoTrack createVideoTrack(VideoCapturer videoCapturer) {
        //Create a VideoSource instance
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        surfaceTextureHelper =
                SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext());
        videoCapturer.initialize(surfaceTextureHelper, getActivity(), videoSource.getCapturerObserver());

        videoCapturer.startCapture(1000, 1000, 30);

        String videoTrackId = "Chat Video Track";
        VideoTrack localVideoTrack = peerConnectionFactory.createVideoTrack(videoTrackId, videoSource);
        localVideoTrack.setEnabled(true);
        return localVideoTrack;
    }

    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        videoCapturer = createCameraCapturer(new Camera1Enumerator(false));
        return videoCapturer;
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // Trying to find a back facing camera!
        Logging.d(TAG, "Looking for back facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // We were not able to find a front cam. Look for other cameras
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    /*
     * Websocket Service
     *
     * */
    private class WebsocketServiceConnection implements ServiceConnection {

        //Called when a connection to the Service has been established, with the IBinder of the communication channel to the Service.
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // ComponentName: The concrete component name of the service that has been connected.
            // IBinder: The IBinder of the Service's communication channel, which you can now make calls on.

            // We've bound to ChatService, cast the IBinder and get ChatService instance
            WebsocketService.WebsocketServiceBinder binder = (WebsocketService.WebsocketServiceBinder)iBinder;
            mWebsocketService = binder.getService();
            // Indicate that we've bound to ChatService
            serviceBound = true;
            websocketServiceResult = mWebsocketService.getObservable();
            websocketServiceResult.subscribe(new DefaultObserver<String>() {
                @Override
                public void onStart() {
                    System.out.println("Start!");
                }
                @Override
                public void onNext(String message) {
                    Log.i(TAG, "received message from websocket service:" + message);
                    handleWebsocketMessage(message);
                }
                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                }
                @Override
                public void onComplete() {
                    System.out.println("Done!");
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mWebsocketService = null;
            // Indicate that we are no longer bound to ChatService
            serviceBound = false;
        }
    }

    private void handleWebsocketMessage(String message) {
        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
        switch (jsonObject.get(AppGeneral.TYPE).getAsString()) {
            case AppGeneral.WEBSOCKET_CONNECTION_CALLBACK:
                String connectionStatus = jsonObject.get(AppGeneral.CONNECTION_STATUS).getAsString();
                if(Objects.equals(connectionStatus, AppGeneral.OPEN)) {
                    websocketConnectionOpened(message);
                }
                break;
            case AppGeneral.READY_FOR_MENTORING_RESPONSE:
                Boolean result = jsonObject.get(AppGeneral.RESULT).getAsBoolean();
                if (result) {
                    showSnackbar(R.string.available_for_chat);
                }
                break;
            case AppGeneral.NOTIFICATION_FOR_WATCH_VIDEO_REQUEST:
                callAction(message);
                break;
            case AppGeneral.WATCH_VIDEO_REQUEST_RESPONSE:
                showWatchVideoHint(message);
                break;
            case AppGeneral.VIDEO_OFFER:
                handleVideoOfferMessage(message);
                break;
            case AppGeneral.VIDEO_ANSWER:
                handleVideoAnswerMessage(message);
                break;
            case AppGeneral.NEW_ICE_CANDIDATE:
                handleNewICECandidateMessage(message);
                break;
            case AppGeneral.TERMINATE_LIVECAST_RESPONSE:
                break;

        }
    }

    private void createMediaConstraintsInternal() {
        // Create audio constraints.
        audioConstraints = new MediaConstraints();
        Log.d(TAG, "Disabling audio processing");
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "true"));
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "true"));
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "true"));
        audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "true"));

        // Create SDP constraints.
        sdpMediaConstraints = new MediaConstraints();
        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
    }

    private PeerConnection.RTCConfiguration createRTCConfigInternal() {
        getIceServers();
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        // Enable DTLS for normal calls and disable for loopback calls.
        rtcConfig.enableDtlsSrtp = true;
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
        return rtcConfig;
    }

    private PeerConnection createPeerConnectionInternal() {
        if (peerConnectionFactory == null) {
            Log.e(TAG, "Peerconnection factory is not created");
            return null;
        }
        Log.d(TAG, "Create peer connection.");
        queuedRemoteCandidates = new ArrayList<>();
        PeerConnection.RTCConfiguration rtcConfig = createRTCConfigInternal();

        // Implementation detail: observe ICE & stream changes and react accordingly.
        final CustomPeerConnectionObserver pcObserver = new CustomPeerConnectionObserver() {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                onLocalIceCandidate(iceCandidate);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                showSnackbar(R.string.remote_stream_received);
                gotRemoteStream(mediaStream);
            }

            @Override
            public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
                super.onAddTrack(rtpReceiver, mediaStreams);
            }

            /*
            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                super.onIceConnectionChange(iceConnectionState);
                if (iceConnectionState.compareTo(PeerConnection.IceConnectionState.CONNECTED) == 0) {
                    VideoTrack videoTrack = remoteMediaStream.videoTracks.get(0);
                    videoTrack.addSink(remoteProxyVideoSink);
                }
            }
            */
        };

        PeerConnection localPeerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, pcObserver);

        for(AudioTrack audioTrack: mediaStream.audioTracks) {
            localPeerConnection.addTrack(audioTrack);
            Log.i(TAG, "Audio Track has been added to the peer connection.");
        }

        for(VideoTrack videoTrack: mediaStream.videoTracks) {
            localPeerConnection.addTrack(videoTrack);
            Log.i(TAG, "Video Track has been added to the peer connection.");
        }

        return localPeerConnection;

    }

    /**
     * Received local ice candidate. Send it to remote peer through signalling for negotiation
     */
    public void onLocalIceCandidate(IceCandidate iceCandidate) {
        //we have received local ice candidate. We can set it to the other peer.
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(AppGeneral.TOKEN, AuthenUtils.peekAuthToken(getActivity().getApplicationContext()));
        jsonObject.addProperty(AppGeneral.TARGET, mTargetUserId.toString());
        jsonObject.addProperty(AppGeneral.TYPE, AppGeneral.NEW_ICE_CANDIDATE);
        jsonObject.add(AppGeneral.CANDIDATE, toJsonCandidate(iceCandidate));
        Log.i(TAG, "To send iceCandidate: " + toJsonCandidate(iceCandidate).toString());
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .create();
        mWebsocketService.sendToWebsocketServer(gson.toJson(jsonObject));
    }

    private void showSnackbar(int stringId) {
        Snackbar.make(mConstraintLayout, stringId, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void getIceServers() {
        List<String> iceServer_urls = new ArrayList<>();
        // this should be fetched from server in a future version
        iceServer_urls.add("turn:13.228.166.130");
        String iceServer_credential = "ninefingers";
        String iceServer_username = "youhavetoberealistic";

        PeerConnection.IceServer peerIceServer = PeerConnection.IceServer.builder(iceServer_urls)
                .setUsername(iceServer_username)
                .setPassword(iceServer_credential)
                .createIceServer();

        PeerConnection.IceServer stunServer = PeerConnection.IceServer.builder("stun:47.95.124.25").createIceServer();
        iceServers.add(peerIceServer);
        // iceServers.add(stunServer);
    }

    //
    private void websocketConnectionOpened(String message) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(AppGeneral.TOKEN, AuthenUtils.peekAuthToken(getActivity()));

        if (mBeingMentor) {
            jsonObject.addProperty(AppGeneral.TYPE, AppGeneral.READY_FOR_MENTORING);
            jsonObject.addProperty(AppGeneral.TARGET, AppGeneral.GENERAL_PUBLIC);
        } else {
            jsonObject.addProperty(AppGeneral.TYPE, AppGeneral.WATCH_VIDEO_REQUEST);
            jsonObject.addProperty(AppGeneral.TARGET, mMentorUserId.toString());
        }

        jsonObject.add(AppGeneral.SDP, JsonNull.INSTANCE);

        Gson gson = new GsonBuilder()
                .serializeNulls()
                .create();
        mWebsocketService.sendToWebsocketServer(gson.toJson(jsonObject));
    }

    // creates peer connection.
    private void callAction(String message) {
        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
        mTargetUserId = jsonObject.get(AppGeneral.TARGET).getAsInt();

        isInitiator = true;

        localPeerConnection = createPeerConnectionInternal();

        // localPeerConnection.createOffer(new MySdpCreateObserver(), sdpMediaConstraints);
    }

    // 观看直播
    private void handleVideoOfferMessage(String message) {
        isInitiator = false;

        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
        localPeerConnection = createPeerConnectionInternal();
        String description = jsonObject.get(AppGeneral.SDP).getAsString();
        mTargetUserId = jsonObject.get(AppGeneral.TARGET).getAsInt();
        // Log.i(TAG, "description from remote server: " + description);
        SessionDescription sdpOffer = new SessionDescription(SessionDescription.Type.fromCanonicalForm("offer"), description);
        Log.i(TAG, "description from remote server has been created: " + sdpOffer.description);
        localPeerConnection.setRemoteDescription(sdpObserver, sdpOffer);
        localPeerConnection.createAnswer(sdpObserver, sdpMediaConstraints);
    }

    private void handleVideoAnswerMessage(String message) {
        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
        String description = jsonObject.get(AppGeneral.SDP).getAsString();
        Integer clientId = jsonObject.get(AppGeneral.TARGET).getAsInt();
        // Log.i(TAG, "description from remote server: " + description);
        SessionDescription sdpOffer = new SessionDescription(SessionDescription.Type.fromCanonicalForm("answer"), description);
        Log.i(TAG, "description from remote server has been created: " + sdpOffer.description);
        localPeerConnection.setRemoteDescription(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {

            }

            @Override
            public void onSetSuccess() {
                Log.i(TAG, "localPeerConnection setRemoteDescription success.");
            }

            @Override
            public void onCreateFailure(String s) {

            }

            @Override
            public void onSetFailure(String s) {
                Log.i(TAG, "localPeerConnection setRemoteDescription failure." + s);
            }
        }, sdpOffer);
        // inspectorPeerConnection.setRemoteDescription(null, sdpOffer);
    }

    private class MySdpObserver implements SdpObserver {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Log.i(TAG, "localPeerConnection setLocalDescription start.");
            localSdp = sessionDescription;
            localPeerConnection.setLocalDescription(sdpObserver, sessionDescription);
        }

        @Override
        public void onSetSuccess() {
            Log.i(TAG, "localPeerConnection set SessionDescription onSetSuccess.");
            if (localPeerConnection.getLocalDescription() != null) {
                // 证明是localDescription被set
                Log.i(TAG, "localPeerConnection setLocalDescription success.");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(AppGeneral.TOKEN, AuthenUtils.peekAuthToken(getActivity().getApplicationContext()));
                jsonObject.addProperty(AppGeneral.TARGET, mTargetUserId);

                if (isInitiator) {
                    jsonObject.addProperty(AppGeneral.TYPE, AppGeneral.VIDEO_OFFER);
                } else {
                    jsonObject.addProperty(AppGeneral.TYPE, AppGeneral.VIDEO_ANSWER);
                }

                jsonObject.addProperty(AppGeneral.SDP, localSdp.description);
                Gson gson = new GsonBuilder()
                        .serializeNulls()
                        .create();
                mWebsocketService.sendToWebsocketServer(gson.toJson(jsonObject));
            }

            if (localPeerConnection.getRemoteDescription() != null) {
                // 证明是remoteDescription被set
                Log.i(TAG, "localPeerConnection setRemoteDescription success.");
            }
        }

        @Override
        public void onCreateFailure(String s) {
            Log.i(TAG, "localPeerConnection onCreateFailure.");
        }

        @Override
        public void onSetFailure(String s) {
            Log.i(TAG, "localPeerConnection set SessionDescription failure." + s);
        }
    }

    private void handleNewICECandidateMessage(String message) {
        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
        JsonObject iceCandidateJsonObject = jsonObject.get(AppGeneral.CANDIDATE).getAsJsonObject();
        String sdp = iceCandidateJsonObject.get(AppGeneral.CANDIDATE).getAsString();
        String sdpMid = iceCandidateJsonObject.get(AppGeneral.SDP_M_ID).getAsString();
        Integer sdpMLineIndex = iceCandidateJsonObject.get(AppGeneral.SDP_M_LINE_INDEX).getAsInt();
        Log.i(TAG, "adding received ICE candidate: " + iceCandidateJsonObject);

        IceCandidate iceCandidate = new IceCandidate(sdpMid, sdpMLineIndex, sdp);
        //
        if (localPeerConnection != null) {
            Log.i(TAG, "localPeerConnection is already created. Now adding received ICE candidate from inspector");
            localPeerConnection.addIceCandidate(iceCandidate);
        } else {
            Log.i(TAG, "localPeerConnection has not been created. yet. Now adding received ICE candidate to queuedLocalCandidates");
            queuedLocalCandidates.add(iceCandidate);
        }
    }



    private void gotRemoteStream(MediaStream mediaStream) {
        VideoTrack videoTrack = mediaStream.videoTracks.get(0);
        AudioTrack audioTrack = mediaStream.audioTracks.get(0);
        // remoteMediaStream = mediaStream;
        videoTrack.setEnabled(true);
        audioTrack.setEnabled(true);
        videoTrack.addSink(remoteProxyVideoSink);
    }

    private void showWatchVideoHint(String message) {
        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
        Boolean findVideo = jsonObject.get(AppGeneral.FIND_VIDEO_SUCCESS).getAsBoolean();
        if (findVideo) {
            showSnackbar(R.string.connecting_video_call_in_progress);
        } else {
            showSnackbar(R.string.video_call_failed);
        }
    }

    // Converts a Java IceCandidate to a JsonObject.
    private JsonObject toJsonCandidate(final IceCandidate iceCandidate) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(AppGeneral.CANDIDATE, iceCandidate.sdp);
        jsonObject.addProperty(AppGeneral.SDP_M_ID, iceCandidate.sdpMid);
        jsonObject.addProperty(AppGeneral.SDP_M_LINE_INDEX, iceCandidate.sdpMLineIndex);
        return jsonObject;
    }
}
