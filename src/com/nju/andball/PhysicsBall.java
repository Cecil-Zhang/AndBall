package com.nju.andball;

import static org.andengine.extension.physics.box2d.util.constants.PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;

import java.io.IOException;
import java.util.Random;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.Entity;
import org.andengine.entity.modifier.RotationModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.shape.IAreaShape;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.controller.MultiTouch;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.debug.Debug;
import org.andengine.util.math.MathUtils;

import android.graphics.Color;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.util.Log;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class PhysicsBall extends SimpleBaseGameActivity implements
		IAccelerationListener {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	private static final FixtureDef BALL_FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(0.1f, 0.8f, 0.6f); // 密度，弹性系数，摩擦系数
	private static final FixtureDef WOOD_FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(1, 2.0f, 0.6f);
	private static final int LAYER_COUNT = 3;

	private static final int LAYER_BACKGROUND = 0;
	private static final int LAYER_SPRITE = LAYER_BACKGROUND + 1;
	private static final int LAYER_SCORE = LAYER_SPRITE + 1;
	private float EndingTimer = 60f;

	// ===========================================================
	// Fields
	// ===========================================================
	protected boolean mGameRunning;
	private Camera mCamera;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TiledTextureRegion mCircleFaceTextureRegion;
	private TiledTextureRegion mFireTexutreRegion;
	private ITextureRegion mWoodTextureRegion;
	private ITextureRegion mNailTextureRegion;
	private ITextureRegion mCloudTextureRegion;
	private BitmapTextureAtlas mBackgroundTexture;
	private ITextureRegion mBackgroundTextureRegion;
	private BitmapTextureAtlas mOnScreenControlTexture;
	private ITextureRegion mOnScreenControlBaseTextureRegion;
	private ITextureRegion mOnScreenControlKnobTextureRegion;

	private boolean mPlaceOnScreenControlsAtDifferentVerticalLocations = false;
	private Scene mScene;
	private Sound mHitSound;
	private Music mBackgroundMusic;

	private PhysicsWorld mPhysicsWorld;
	private Sprite wood;
	private Sprite nail;
	private AnimatedSprite ball;
	private Body woodBody;
	private Body ballBody;
	private int mScore = 0;
	private Text mScoreText;
	private Text mTimerText;
	private Text mGameOverText;
	private Font mFont;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public EngineOptions onCreateEngineOptions() {

		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		final EngineOptions engineOptions = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE_FIXED, new FillResolutionPolicy(), this.mCamera);
		engineOptions.getTouchOptions().setNeedsMultiTouch(true);
		if (MultiTouch.isSupported(this)) {
			if (MultiTouch.isSupportedDistinct(this)) {
				Toast.makeText(
						this,
						"MultiTouch detected --> Both controls will work properly!",
						Toast.LENGTH_SHORT).show();
			} else {
				this.mPlaceOnScreenControlsAtDifferentVerticalLocations = true;
				Toast.makeText(
						this,
						"MultiTouch detected, but your device has problems distinguishing between fingers.\n\nControls are placed at different vertical locations.",
						Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(
					this,
					"Sorry your device does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)\n\nControls are placed at different vertical locations.",
					Toast.LENGTH_LONG).show();
		}
		engineOptions.getAudioOptions().setNeedsSound(true);
		engineOptions.getAudioOptions().setNeedsMusic(true);
		return engineOptions;
	}

	@Override
	public void onCreateResources() {
		/* Load the font we are going to use. */
		FontFactory.setAssetBasePath("fonts/");
		this.mFont = FontFactory.createFromAsset(this.getFontManager(),
				this.getTextureManager(), 512, 512, TextureOptions.BILINEAR,
				this.getAssets(), "Plok.ttf", 32, true, Color.WHITE);
		this.mFont.load();

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("sprite/");

		this.mBitmapTextureAtlas = new BitmapTextureAtlas(
				this.getTextureManager(), 712, 712, TextureOptions.BILINEAR);
		this.mCircleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.mBitmapTextureAtlas, this,
						"face_circle_tiled.png", 0, 0, 2, 1); // 64x32
		this.mWoodTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "wood.png", 0,
						32);	//  128x18
		this.mNailTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this,
						"nail_down.png", 0, 64);  // 26x59
		this.mFireTexutreRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.mBitmapTextureAtlas, this, "fire.png", 0, 128, 6, 2); //  360x356
		this.mOnScreenControlTexture = new BitmapTextureAtlas(
				this.getTextureManager(), 256, 128, TextureOptions.BILINEAR);
		this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mOnScreenControlTexture, this,
						"onscreen_control_base.png", 0, 0);
		this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mOnScreenControlTexture, this,
						"onscreen_control_knob.png", 128, 0);
		this.mOnScreenControlTexture.load();

		this.mBitmapTextureAtlas.load();
		
		this.mBackgroundTexture = new BitmapTextureAtlas(this.getTextureManager(), 1024, 2048);
		this.mBackgroundTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBackgroundTexture, this, "background.jpg", 0, 0);
		this.mCloudTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBackgroundTexture, this, "cloud.png", 0, 768);
		this.mBackgroundTexture.load();
		
		SoundFactory.setAssetBasePath("music/");
		try {
			this.mHitSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "ballRebound.wav");
		} catch (final IOException e) {
			Debug.e(e);
		}
		
		MusicFactory.setAssetBasePath("music/");
		try {
			this.mBackgroundMusic = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "quitVillage.mid");
			this.mBackgroundMusic.setLooping(true);
		} catch (final IOException e) {
			Debug.e(e);
		}
	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
		for (int i = 0; i < LAYER_COUNT; i++) {
			this.mScene.attachChild(new Entity());
		}
//		this.mScene.setBackgroundEnabled(false);
//		this.mScene.getChildByIndex(LAYER_BACKGROUND).attachChild(new Sprite(0, 0, this.mBackgroundTextureRegion, this.getVertexBufferObjectManager()));
		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(0.0f, new Sprite(0, CAMERA_HEIGHT - this.mBackgroundTextureRegion.getHeight(), this.mBackgroundTextureRegion, this.getVertexBufferObjectManager())));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f, new Sprite(0, 80, this.mCloudTextureRegion, this.getVertexBufferObjectManager())));
		this.mScene.setBackground(autoParallaxBackground);
		
		// 创建一个物理世界，重力与地球重力相等
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0,
				SensorManager.GRAVITY_MARS), false);

		this.mBackgroundMusic.play();
		this.initSprites();
		this.addBall();
		this.initOnScreenControls();
		this.initText();

		return this.mScene;
	}

	private void initSprites() {
		final VertexBufferObjectManager vertexBufferObjectManager = this
				.getVertexBufferObjectManager();
		// 创建物理世界边界
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2,
				CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2,
				vertexBufferObjectManager);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT,
				vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2,
				CAMERA_HEIGHT, vertexBufferObjectManager);

		// 物理世界边界材料定义，并根据材料创建刚体
		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0,
				0.7f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground,
				BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof,
				BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left,
				BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right,
				BodyType.StaticBody, wallFixtureDef);

		// 创建木板
		wood = new Sprite(CAMERA_WIDTH / 2, CAMERA_HEIGHT - 64,
				this.mWoodTextureRegion, this.getVertexBufferObjectManager());
		woodBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, wood,
				BodyType.KinematicBody, WOOD_FIXTURE_DEF); // KinematicBody根据速度进行移动，但不受重力影响

		// 创建钉子
		nail = new Sprite(CAMERA_WIDTH / 2, 0, this.mNailTextureRegion,
				this.getVertexBufferObjectManager());
		this.mScene.getChildByIndex(LAYER_BACKGROUND).attachChild(nail);
		
		// 创建火焰
		final AnimatedSprite fire = new AnimatedSprite(320, CAMERA_HEIGHT-64, this.mFireTexutreRegion, this.getVertexBufferObjectManager());
		fire.animate(100);

		// 将精灵加入到场景中
		this.mScene.getChildByIndex(LAYER_SPRITE).attachChild(ground);
		this.mScene.getChildByIndex(LAYER_SPRITE).attachChild(roof);
		this.mScene.getChildByIndex(LAYER_SPRITE).attachChild(left);
		this.mScene.getChildByIndex(LAYER_SPRITE).attachChild(right);
		this.mScene.getChildByIndex(LAYER_SPRITE).attachChild(wood);
		this.mScene.getChildByIndex(LAYER_BACKGROUND).attachChild(fire);

		// 创建刚体与精灵的物理连接件，并允许刚体和物理世界改变精灵位置，两个操控版都是靠改变刚体状态来间接改变精灵状态
		this.mScene.setTouchAreaBindingOnActionDownEnabled(true);
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(wood,
				woodBody, true, true));

		// 注册木板与左右边界的碰撞检测，检测到碰撞时反弹木板（KinematicBody与StaticBody不会发生碰撞）
		mScene.registerUpdateHandler(new IUpdateHandler() {
			private float noSoundTime=0;
			
			@Override
			public void reset() {
			}

			@Override
			public void onUpdate(final float pSecondsElapsed) {
				EndingTimer -= pSecondsElapsed;
				if(EndingTimer<=0){
					mScene.unregisterUpdateHandler(this);
					mTimerText.setText("Time: 0s");
					onGameOver();
				}else{
					mTimerText.setText("Time: "+String.valueOf(Math.round(EndingTimer))+"s");
				}
				
				if (left.collidesWith(wood)) {
					woodBody.setLinearVelocity(2, 0);
				}

				if (right.collidesWith(wood)) {
					woodBody.setLinearVelocity(-2, 0);
				}
				
				if((this.noSoundTime>0.5) && (ball.collidesWith(roof) || ball.collidesWith(left) || ball.collidesWith(right) || ball.collidesWith(ground) ||ball.collidesWith(wood))){
					PhysicsBall.this.mHitSound.play();
					this.noSoundTime=0;
				}else{
					this.noSoundTime+=pSecondsElapsed;
				}

				if (nail.collidesWith(ball)) {
					removeBall();
					mScore = mScore + 50;
					mScoreText.setText("Score: " + mScore);
					moveNail();
					addBall();
					
				}

				// if(ground.collidesWith(face)){
				// onGameOver();
				// }
			}
		});
	}

	private void moveNail(){
		int seed=(int) Math.round( Math.random()*4);
		switch(seed%4){
		case 0:
			//move nail to roof
			nail.setPosition(((float) Math.random())*CAMERA_WIDTH, 0);
			nail.setRotation(0);
			break;
		case 1:
			//move nail to left
			nail.setPosition(10, ((float) Math.random())*CAMERA_HEIGHT);
			nail.setRotation(270f);
			break;
		case 2:
			//move nail to right
			nail.setPosition(CAMERA_WIDTH-30, ((float) Math.random())*CAMERA_HEIGHT);
			nail.setRotation(90f);
			break;
		case 3:
			nail.setPosition(((float) Math.random())*CAMERA_WIDTH, CAMERA_HEIGHT-64);
			nail.setRotation(180f);
		}
	}
	
	private void removeBall() {
		final PhysicsConnector facePhysicsConnector = mPhysicsWorld
				.getPhysicsConnectorManager().findPhysicsConnectorByShape(ball);

		mPhysicsWorld.unregisterPhysicsConnector(facePhysicsConnector);
		mPhysicsWorld.destroyBody(facePhysicsConnector.getBody());

		mScene.unregisterTouchArea(ball);
		mScene.getChildByIndex(LAYER_SPRITE).detachChild(ball);

		System.gc();
	}

	private void addBall() {
		// 创建小球
		ball = new AnimatedSprite(((float) Math.random())*CAMERA_WIDTH, ((float) Math.random())*CAMERA_HEIGHT, this.mCircleFaceTextureRegion,
				this.getVertexBufferObjectManager());
		ballBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, ball,
				BodyType.DynamicBody, BALL_FIXTURE_DEF);

		ball.animate(200);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(ball,
				ballBody, true, true));
		this.mScene.getChildByIndex(LAYER_SPRITE).attachChild(ball);
	}
	

	private void initOnScreenControls() {
		// 创建操控版，左操控版控制木板x轴速度，右操控版控制木板旋转的角速度
		/* Velocity control (left). */
		final float x1 = 0;
		final float y1 = CAMERA_HEIGHT
				- this.mOnScreenControlBaseTextureRegion.getHeight();
		final AnalogOnScreenControl velocityOnScreenControl = new AnalogOnScreenControl(
				x1, y1, this.mCamera, this.mOnScreenControlBaseTextureRegion,
				this.mOnScreenControlKnobTextureRegion, 0.1f,
				this.getVertexBufferObjectManager(),
				new IAnalogOnScreenControlListener() {
					@Override
					public void onControlChange(
							final BaseOnScreenControl pBaseOnScreenControl,
							final float pValueX, final float pValueY) {
						final Vector2 velocity = Vector2Pool.obtain(
								pValueX * 50, 0);
						woodBody.setLinearVelocity(velocity);
						Vector2Pool.recycle(velocity);
					}

					@Override
					public void onControlClick(
							final AnalogOnScreenControl pAnalogOnScreenControl) {
						/* Nothing. */
					}
				});
		velocityOnScreenControl.getControlBase().setBlendFunction(
				GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		velocityOnScreenControl.getControlBase().setAlpha(0.5f);

		this.mScene.setChildScene(velocityOnScreenControl);

		/* Rotation control (right). */
		final float y2 = (this.mPlaceOnScreenControlsAtDifferentVerticalLocations) ? 0
				: y1;
		final float x2 = CAMERA_WIDTH
				- this.mOnScreenControlBaseTextureRegion.getWidth();
		final AnalogOnScreenControl rotationOnScreenControl = new AnalogOnScreenControl(
				x2, y2, this.mCamera, this.mOnScreenControlBaseTextureRegion,
				this.mOnScreenControlKnobTextureRegion, 0.1f,
				this.getVertexBufferObjectManager(),
				new IAnalogOnScreenControlListener() {
					@Override
					public void onControlChange(
							final BaseOnScreenControl pBaseOnScreenControl,
							final float pValueX, final float pValueY) {
						if (pValueX == x1 && pValueY == x1) {
							// woodBody.setAngularVelocity(x1/50);
							final float rotationInRad = (float) Math.atan2(x1,
									0);
							woodBody.setTransform(woodBody.getWorldCenter(),
									rotationInRad);

							PhysicsBall.this.wood.setRotation(MathUtils
									.radToDeg(rotationInRad));
						} else {
							// woodBody.setAngularVelocity(MathUtils.radToDeg((float)
							// Math.atan2(pValueX/50, -pValueY/50)));
							final float rotationInRad = (float) Math.atan2(
									pValueX / 50, -pValueY / 50);
							woodBody.setTransform(woodBody.getWorldCenter(),
									rotationInRad);

							PhysicsBall.this.wood.setRotation(MathUtils
									.radToDeg(rotationInRad));
						}
						;
					}

					@Override
					public void onControlClick(
							final AnalogOnScreenControl pAnalogOnScreenControl) {
						/* Nothing. */
					}
				});
		rotationOnScreenControl.getControlBase().setBlendFunction(
				GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		rotationOnScreenControl.getControlBase().setAlpha(0.5f);

		velocityOnScreenControl.setChildScene(rotationOnScreenControl);
	}

	private void initText() {
		/* The ScoreText showing how many points the pEntity scored. */
		this.mTimerText = new Text(CAMERA_WIDTH*6/10,5,this.mFont,"Time:60s","Time:60s".length(),this.getVertexBufferObjectManager());
		this.mTimerText.setBlendFunction(GLES20.GL_SRC_ALPHA,
				GLES20.GL_ONE_MINUS_SRC_ALPHA);
		this.mTimerText.setAlpha(0.5f);
		this.mScene.getChildByIndex(LAYER_SCORE).attachChild(mTimerText);
		
		this.mScoreText = new Text(5, 5, this.mFont, "Score: 0",
				"Score: XXXX".length(), this.getVertexBufferObjectManager());
		this.mScoreText.setBlendFunction(GLES20.GL_SRC_ALPHA,
				GLES20.GL_ONE_MINUS_SRC_ALPHA);
		this.mScoreText.setAlpha(0.5f);
		this.mScene.getChildByIndex(LAYER_SCORE).attachChild(this.mScoreText);

		/* The game-over text. */
		this.mGameOverText = new Text(0, 0, this.mFont, "Game\nOver",
				new TextOptions(HorizontalAlign.CENTER),
				this.getVertexBufferObjectManager());
		this.mGameOverText.setPosition(
				(CAMERA_WIDTH - this.mGameOverText.getWidth()) * 0.5f,
				(CAMERA_HEIGHT - this.mGameOverText.getHeight()) * 0.5f);
		this.mGameOverText.registerEntityModifier(new ScaleModifier(3, 0.1f,
				2.0f));
		this.mGameOverText.registerEntityModifier(new RotationModifier(3, 0,
				720));
	}

	@Override
	public void onAccelerationAccuracyChanged(
			final AccelerationData pAccelerationData) {

	}

	@Override
	public void onAccelerationChanged(final AccelerationData pAccelerationData) {
		// 根据手机重力感应器的状态改变物理世界的重力
		final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX(),
				pAccelerationData.getY());
		this.mPhysicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
	}

	@Override
	public void onResumeGame() {
		super.onResumeGame();

		this.enableAccelerationSensor(this);
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();

		this.disableAccelerationSensor();
	}

	private void onGameOver() {
		// this.mScene.getChildByIndex(LAYER_SCORE).attachChild(this.mGameOverText);
		this.ballBody.setLinearVelocity(0, 0);
		this.mGameRunning = false;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * Creates a {@link Body} based on a {@link PolygonShape} in the form of a
	 * triangle:
	 * 
	 * <pre>
	 *  /\
	 * /__\
	 * </pre>
	 */
	private static Body createTriangleBody(final PhysicsWorld pPhysicsWorld,
			final IAreaShape pAreaShape, final BodyType pBodyType,
			final FixtureDef pFixtureDef) {
		/*
		 * Remember that the vertices are relative to the center-coordinates of
		 * the Shape.
		 */
		final float halfWidth = pAreaShape.getWidthScaled() * 0.5f
				/ PIXEL_TO_METER_RATIO_DEFAULT;
		final float halfHeight = pAreaShape.getHeightScaled() * 0.5f
				/ PIXEL_TO_METER_RATIO_DEFAULT;

		final float top = -halfHeight;
		final float bottom = halfHeight;
		final float left = -halfHeight;
		final float centerX = 0;
		final float right = halfWidth;

		final Vector2[] vertices = { new Vector2(centerX, top),
				new Vector2(right, bottom), new Vector2(left, bottom) };

		return PhysicsFactory.createPolygonBody(pPhysicsWorld, pAreaShape,
				vertices, pBodyType, pFixtureDef);
	}

	/**
	 * Creates a {@link Body} based on a {@link PolygonShape} in the form of a
	 * hexagon:
	 * 
	 * <pre>
	 *  /\
	 * /  \
	 * |  |
	 * |  |
	 * \  /
	 *  \/
	 * </pre>
	 */
	private static Body createHexagonBody(final PhysicsWorld pPhysicsWorld,
			final IAreaShape pAreaShape, final BodyType pBodyType,
			final FixtureDef pFixtureDef) {
		/*
		 * Remember that the vertices are relative to the center-coordinates of
		 * the Shape.
		 */
		final float halfWidth = pAreaShape.getWidthScaled() * 0.5f
				/ PIXEL_TO_METER_RATIO_DEFAULT;
		final float halfHeight = pAreaShape.getHeightScaled() * 0.5f
				/ PIXEL_TO_METER_RATIO_DEFAULT;

		/*
		 * The top and bottom vertex of the hexagon are on the bottom and top of
		 * hexagon-sprite.
		 */
		final float top = -halfHeight;
		final float bottom = halfHeight;

		final float centerX = 0;

		/*
		 * The left and right vertices of the heaxgon are not on the edge of the
		 * hexagon-sprite, so we need to inset them a little.
		 */
		final float left = -halfWidth + 2.5f / PIXEL_TO_METER_RATIO_DEFAULT;
		final float right = halfWidth - 2.5f / PIXEL_TO_METER_RATIO_DEFAULT;
		final float higher = top + 8.25f / PIXEL_TO_METER_RATIO_DEFAULT;
		final float lower = bottom - 8.25f / PIXEL_TO_METER_RATIO_DEFAULT;

		final Vector2[] vertices = { new Vector2(centerX, top),
				new Vector2(right, higher), new Vector2(right, lower),
				new Vector2(centerX, bottom), new Vector2(left, lower),
				new Vector2(left, higher) };

		return PhysicsFactory.createPolygonBody(pPhysicsWorld, pAreaShape,
				vertices, pBodyType, pFixtureDef);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
