package com.nju.andball;

import java.io.IOException;
import java.util.ArrayList;

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
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.DelayModifier;
import org.andengine.entity.modifier.FadeInModifier;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.modifier.ParallelEntityModifier;
import org.andengine.entity.modifier.RotationByModifier;
import org.andengine.entity.modifier.RotationModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
import org.andengine.entity.modifier.LoopEntityModifier.ILoopEntityModifierListener;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.AnimatedSprite.IAnimationListener;
import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.touch.controller.MultiTouch;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleAsyncGameActivity;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.debug.Debug;
import org.andengine.util.math.MathUtils;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.modifier.LoopModifier;
import org.andengine.util.modifier.ease.EaseBounceIn;
import org.andengine.util.progress.IProgressListener;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.util.Log;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class PhysicsBall extends SimpleAsyncGameActivity {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	private static final FixtureDef BALL_FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(0.1f, 0.7f, 0.6f); // 密度，弹性系数，摩擦系数
	private static final FixtureDef WOOD_FIXTURE_DEF = PhysicsFactory
			.createFixtureDef(1, 1.8f, 0.6f);
	private static final int LAYER_COUNT = 3;

	private static final int LAYER_BACKGROUND = 0;
	private static final int LAYER_SPRITE = LAYER_BACKGROUND + 1;
	private static final int LAYER_SCORE = LAYER_SPRITE + 1;
	private float EndingTimer = 60f;

	// ===========================================================
	// Fields
	// ===========================================================
	protected boolean mGameRunning;
	private boolean soundEnabled;
	private Camera mCamera;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TiledTextureRegion mBallTextureRegion;
	private TiledTextureRegion mFireTextureRegion;
	private TiledTextureRegion mBoomTextureRegion;
	private TiledTextureRegion mCoinTextureRegion;
	private ITextureRegion mWoodTextureRegion;
	private ITextureRegion mNailTextureRegion;
	private ITextureRegion mCloudTextureRegion;
	private ITextureRegion mReplayTextureRegion;
	private ITextureRegion mReplayPressedTextureRegion;
	private BitmapTextureAtlas mBackgroundTexture;
	private ITextureRegion mBackgroundTextureRegion;
	private BitmapTextureAtlas mOnScreenControlTexture;
	private ITextureRegion mOnScreenControlBaseTextureRegion;
	private ITextureRegion mOnScreenControlKnobTextureRegion;

	private boolean mPlaceOnScreenControlsAtDifferentVerticalLocations = false;
	private Scene mScene;
	private Sound mHitSound;
	private Sound mCoinSound;
	private Sound boomSound;
	private Sound gameOverSound;
	private Music mBackgroundMusic;

	private PhysicsWorld mPhysicsWorld;
	private Sprite wood;
	private Sprite nail;
	private AnimatedSprite ball;
	private ArrayList<AnimatedSprite> fires;
	private ArrayList<AnimatedSprite> coins;
	private Body woodBody;
	private Body ballBody;
	private int mScore = 0;
	private int bestScore = 0;
	private Text mScoreText;
	private Text mTimerText;
	private Text mGameOverText;
	private Font mFont;
	private LoopEntityModifier entityModifier;
	private IUpdateHandler collideHandler;
	private final int COIN_NUMBER = 20;

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
				ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
		engineOptions.getTouchOptions().setNeedsMultiTouch(true);
		if (MultiTouch.isSupported(this)) {
			if (MultiTouch.isSupportedDistinct(this)) {
//				Toast.makeText(
//						this,
//						"MultiTouch detected --> Both controls will work properly!",
//						Toast.LENGTH_SHORT).show();
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
		if (Constants.getInstance(this).getSoundEnabled()) {
			engineOptions.getAudioOptions().setNeedsSound(true);
			engineOptions.getAudioOptions().setNeedsMusic(true);
			this.soundEnabled = true;
		} else {
			this.soundEnabled = false;
		}
		this.bestScore = Constants.getInstance(this).getHighScore();
		engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
		return engineOptions;
	}

	@Override
	public void onCreateResourcesAsync(final IProgressListener pProgressListener) {
		/* Load the font we are going to use. */
		pProgressListener.onProgressChanged(0);
		FontFactory.setAssetBasePath("fonts/");
		this.mFont = FontFactory.createFromAsset(this.getFontManager(),
				this.getTextureManager(), 512, 512, TextureOptions.BILINEAR,
				this.getAssets(), "DeadSpaceTitleFont.ttf", 45, true,
				Color.WHITE);
		this.mFont.load();
		pProgressListener.onProgressChanged(10);
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("sprite/");

		this.mBitmapTextureAtlas = new BitmapTextureAtlas(
				this.getTextureManager(), 1024, 512, TextureOptions.BILINEAR);
		// 第一行, 685 x 65
		this.mWoodTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "wood.png", 0,
						0); // 128x18
		this.mNailTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "dragon.png",
						504, 0); // 53 x 65
		this.mCoinTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.mBitmapTextureAtlas, this,
						"smallCoin.png", 154, 0, 4, 2); // 126x65
		this.mBallTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.mBitmapTextureAtlas, this,
						"DragonBalls.png", 280, 0, 3, 1); // 224 x 32
		this.mReplayTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "retry.png",
						557, 0); // 64x64
		this.mReplayPressedTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "retry1.png",
						621, 0); // 64x64
		pProgressListener.onProgressChanged(30);
		// 第二行， 680 x 366
		this.mFireTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.mBitmapTextureAtlas, this,
						"fire.png", 0, 65, 6, 2); // 360x356
		this.mBoomTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.mBitmapTextureAtlas, this,
						"boom.png", 360, 65, 5, 2); // 320x366

		pProgressListener.onProgressChanged(50);
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
		pProgressListener.onProgressChanged(70);

		this.mBackgroundTexture = new BitmapTextureAtlas(
				this.getTextureManager(), 1024, 1024);
		this.mBackgroundTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBackgroundTexture, this,
						"background.jpg", 0, 0); // 1024x768
		this.mCloudTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBackgroundTexture, this, "cloud.png", 0,
						768); // 999x250
		this.mBackgroundTexture.load();
		pProgressListener.onProgressChanged(90);

		if (this.soundEnabled) {
			SoundFactory.setAssetBasePath("music/");
			try {
				this.mHitSound = SoundFactory
						.createSoundFromAsset(this.mEngine.getSoundManager(),
								this, "ballRebound.wav");
				this.mCoinSound = SoundFactory.createSoundFromAsset(
						this.mEngine.getSoundManager(), this, "coin.mp3");
				this.boomSound = SoundFactory.createSoundFromAsset(
						this.mEngine.getSoundManager(), this, "dragonball.wav");
				this.gameOverSound = SoundFactory.createSoundFromAsset(
						this.mEngine.getSoundManager(), this, "game_over.ogg");
			} catch (final IOException e) {
				Debug.e(e);
			}

			MusicFactory.setAssetBasePath("music/");
			try {
				this.mBackgroundMusic = MusicFactory
						.createMusicFromAsset(this.mEngine.getMusicManager(),
								this, "quitVillage.mid");
				this.mBackgroundMusic.setLooping(true);
			} catch (final IOException e) {
				Debug.e(e);
			}
		}
		pProgressListener.onProgressChanged(100);

	}

	@Override
	public Scene onCreateSceneAsync(final IProgressListener pProgressListener) {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
		for (int i = 0; i < LAYER_COUNT; i++) {
			this.mScene.attachChild(new Entity());
		}
		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(
				0, 0, 0, 5);
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(0.0f,
				new Sprite(0, CAMERA_HEIGHT
						- this.mBackgroundTextureRegion.getHeight(),
						this.mBackgroundTextureRegion, this
								.getVertexBufferObjectManager())));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f,
				new Sprite(0, 80, this.mCloudTextureRegion, this
						.getVertexBufferObjectManager())));
		this.mScene.setBackground(autoParallaxBackground);

		// 创建一个物理世界，重力与地球重力相等
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0,
				SensorManager.GRAVITY_MARS), false);
		this.initSprites();
		this.initOnScreenControls();
		this.initText();
		this.onGameStart();
		return this.mScene;
	}

	@Override
	public void onPopulateSceneAsync(Scene pScene,
			IProgressListener pProgressListener) throws Exception {
		// TODO Auto-generated method stub

	}

//	@Override
//	public void onAccelerationAccuracyChanged(
//			final AccelerationData pAccelerationData) {
//
//	}
//
//	@Override
//	public void onAccelerationChanged(final AccelerationData pAccelerationData) {
//		// 根据手机重力感应器的状态改变物理世界的重力
//		final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX(),
//				pAccelerationData.getY());
//		this.mPhysicsWorld.setGravity(gravity);
//		Vector2Pool.recycle(gravity);
//	}

	@Override
	public void onResumeGame() {
		super.onResumeGame();
		if(this.mBackgroundMusic!=null && this.mBackgroundMusic.isPlaying()){
			this.mBackgroundMusic.pause();
		}
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();
		if(this.soundEnabled){
			this.mBackgroundMusic.resume();
		}
	}

	@Override
	public void onBackPressed() {
		Toast.makeText(this, "连按两下退出游戏", Toast.LENGTH_LONG).show();;
		Intent intent = new Intent(this, Menu.class);
		startActivity(intent);
		this.finish();
	}

	// ===========================================================
	// Methods
	// ===========================================================

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
		wood = new Sprite(CAMERA_WIDTH / 2, CAMERA_HEIGHT - 80,
				this.mWoodTextureRegion, this.getVertexBufferObjectManager());
		woodBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, wood,
				BodyType.KinematicBody, WOOD_FIXTURE_DEF); // KinematicBody根据速度进行移动，但不受重力影响

		// 创建钉子
		nail = new Sprite(CAMERA_WIDTH / 2, 0, this.mNailTextureRegion,
				this.getVertexBufferObjectManager());
		this.mScene.getChildByIndex(LAYER_BACKGROUND).attachChild(nail);

		// 创建火焰
		fires = new ArrayList<AnimatedSprite>();
		for (int i = 0; i < 800; i += 100) {
			final AnimatedSprite fire = new AnimatedSprite(20 + i,
					CAMERA_HEIGHT - 48, this.mFireTextureRegion,
					this.getVertexBufferObjectManager());
			fire.animate(100);
			fires.add(fire);
			this.mScene.getChildByIndex(LAYER_BACKGROUND).attachChild(fire);
		}

		// 将精灵加入到场景中
		this.mScene.getChildByIndex(LAYER_SPRITE).attachChild(ground);
		this.mScene.getChildByIndex(LAYER_SPRITE).attachChild(roof);
		this.mScene.getChildByIndex(LAYER_SPRITE).attachChild(left);
		this.mScene.getChildByIndex(LAYER_SPRITE).attachChild(right);
		this.mScene.getChildByIndex(LAYER_SPRITE).attachChild(wood);

		// 创建刚体与精灵的物理连接件，并允许刚体和物理世界改变精灵位置，两个操控版都是靠改变刚体状态来间接改变精灵状态
		this.mScene.setTouchAreaBindingOnActionDownEnabled(true);
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(wood,
				woodBody, true, true));

		// 注册木板与左右边界的碰撞检测，检测到碰撞时反弹木板（KinematicBody与StaticBody不会发生碰撞）
		this.collideHandler = new IUpdateHandler() {
			private float noSoundTime = 0;

			@Override
			public void reset() {
			}

			@Override
			public void onUpdate(final float pSecondsElapsed) {
				if (left.collidesWith(wood)) {
					woodBody.setLinearVelocity(2, 0);
				}

				if (right.collidesWith(wood)) {
					woodBody.setLinearVelocity(-2, 0);
				}

				if (mGameRunning) {
					EndingTimer -= pSecondsElapsed;
					if (soundEnabled) {
						if ((this.noSoundTime > 0.5)
								&& (ball.collidesWith(roof)
										|| ball.collidesWith(left)
										|| ball.collidesWith(right)
										|| ball.collidesWith(ground) || ball
											.collidesWith(wood))) {
							PhysicsBall.this.mHitSound.play();
							this.noSoundTime = 0;
						} else {
							this.noSoundTime += pSecondsElapsed;
						}
					}

					if (EndingTimer <= 0) {
						mScene.unregisterUpdateHandler(this);
						mTimerText.setText("Time: 0s");
						onGameOver();
						return;
					} else {
						mTimerText
								.setText("Time: "
										+ String.valueOf(Math
												.round(EndingTimer)) + "s");
					}

					for (AnimatedSprite fire : fires) {
						if (fire.collidesWith(ball)) {
							onGameOver();
							return;
						}
					}

					if (nail.collidesWith(ball)) {
						removeBall(true);
						mScore = mScore + 50;
						mScoreText.setText("Score: " + mScore);
						moveNail();
						addBall();
					}
				}
			}
		};
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
						if (mGameRunning) {
							final Vector2 velocity = Vector2Pool.obtain(
									pValueX * 35, 0);
							woodBody.setLinearVelocity(velocity);
							Vector2Pool.recycle(velocity);
						}
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
						if (mGameRunning) {
							if (pValueX == x1 && pValueY == x1) {
								// woodBody.setAngularVelocity(x1/50);
								final float rotationInRad = (float) Math.atan2(
										x1, 0);
								woodBody.setTransform(
										woodBody.getWorldCenter(),
										rotationInRad);

								PhysicsBall.this.wood.setRotation(MathUtils
										.radToDeg(rotationInRad));
							} else {
								// woodBody.setAngularVelocity(MathUtils.radToDeg((float)
								// Math.atan2(pValueX/50, -pValueY/50)));
								final float rotationInRad = (float) Math.atan2(
										pValueX / 50, -pValueY / 50);
								woodBody.setTransform(
										woodBody.getWorldCenter(),
										rotationInRad);

								PhysicsBall.this.wood.setRotation(MathUtils
										.radToDeg(rotationInRad));
							}
						}
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
		this.mTimerText = new Text(CAMERA_WIDTH * 6 / 10, 5, this.mFont,
				"Time:60s", "Time:60s".length(),
				this.getVertexBufferObjectManager());
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

	private void createModifier() {
		// 创建实体修改器，在业务线程中更新实体状态
		entityModifier = new LoopEntityModifier(
		// EntityModifier的监听，通知LoopEntityModifier的开始和结束
				new IEntityModifierListener() {
					@Override
					public void onModifierStarted(
							final IModifier<IEntity> pModifier,
							final IEntity pItem) {
						PhysicsBall.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(PhysicsBall.this,
										"Sequence started.", Toast.LENGTH_SHORT)
										.show();
							}
						});
					}

					@Override
					public void onModifierFinished(
							final IModifier<IEntity> pEntityModifier,
							final IEntity pEntity) {
						PhysicsBall.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(PhysicsBall.this,
										"Sequence finished.",
										Toast.LENGTH_SHORT).show();
							}
						});
					}
				}, 2,
				// 循环的监听，通知每次循环的开始和结束
				new ILoopEntityModifierListener() {
					@Override
					public void onLoopStarted(
							final LoopModifier<IEntity> pLoopModifier,
							final int pLoop, final int pLoopCount) {
						PhysicsBall.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(
										PhysicsBall.this,
										"Loop: '" + (pLoop + 1) + "' of '"
												+ pLoopCount + "' started.",
										Toast.LENGTH_SHORT).show();
							}
						});
					}

					@Override
					public void onLoopFinished(
							final LoopModifier<IEntity> pLoopModifier,
							final int pLoop, final int pLoopCount) {
						PhysicsBall.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(
										PhysicsBall.this,
										"Loop: '" + (pLoop + 1) + "' of '"
												+ pLoopCount + "' finished.",
										Toast.LENGTH_SHORT).show();
							}
						});
					}
				},
				// 循环Modifier中组合的Modifier，先按顺序执行
				new SequenceEntityModifier(new AlphaModifier(2, 1, 0),
						new AlphaModifier(1, 0, 1), new ScaleModifier(2, 1,
								0.5f), new DelayModifier(0.5f),
						// 并行执行
						new ParallelEntityModifier(
								new ScaleModifier(3, 0.5f, 5),
								new RotationByModifier(3, 90)),
						new ParallelEntityModifier(new ScaleModifier(3, 5, 1),
								new RotationModifier(3, 180, 0))));
	}

	private void moveNail() {
		int seed = (int) Math.round(Math.random() * 3);
		float x, y;
		switch (seed % 3) {
		case 0:
			// move nail to roof
			x = ((float) Math.random()) * CAMERA_WIDTH;
			nail.setPosition(x, 0);
			Log.i("nail", String.valueOf(x) + ", 0");
			break;
		case 1:
			// move nail to left
			y = ((float) Math.random() * CAMERA_HEIGHT * 2 / 3);
			nail.setPosition(0, y);
			Log.i("nail", "10, " + String.valueOf(y));
			break;
		case 2:
			// move nail to right
			y = ((float) Math.random() * CAMERA_HEIGHT * 2 / 3);
			nail.setPosition(CAMERA_WIDTH - nail.getWidth(), y);
			Log.i("nail",
					String.valueOf(CAMERA_WIDTH - 30) + ", "
							+ String.valueOf(y));
			break;
		}
	}

	private void removeBall(boolean ifPlayBoom) {
		if (ball != null) {
			if (ifPlayBoom) {
				displayBoom(ball.getX(), ball.getY());
			}
			final PhysicsConnector facePhysicsConnector = mPhysicsWorld
					.getPhysicsConnectorManager().findPhysicsConnectorByShape(
							ball);

			mPhysicsWorld.unregisterPhysicsConnector(facePhysicsConnector);
			mPhysicsWorld.destroyBody(facePhysicsConnector.getBody());

			mScene.getChildByIndex(LAYER_SPRITE).detachChild(ball);
			ball = null;

			System.gc();
		}

	}

	private void addBall() {
		// 创建小球
		float x = ((float) Math.random()) * CAMERA_WIDTH * 4 / 6 + 50;
		float y = ((float) Math.random()) * 100;
		ball = new AnimatedSprite(x, y, this.mBallTextureRegion,
				this.getVertexBufferObjectManager());
		ballBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, ball,
				BodyType.DynamicBody, BALL_FIXTURE_DEF);

		ball.animate(200);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(ball,
				ballBody, true, true));
		this.mScene.getChildByIndex(LAYER_SPRITE).attachChild(ball);
		Log.i("ball", String.valueOf(x) + ", " + String.valueOf(y));
	}

	private void displayBoom(float x, float y) {
		final AnimatedSprite boom = new AnimatedSprite(x, y - 160,
				this.mBoomTextureRegion, this.getVertexBufferObjectManager());
		if (soundEnabled) {
			boomSound.play();
		}
		boom.animate(50, false, new IAnimationListener() {

			@Override
			public void onAnimationStarted(AnimatedSprite pAnimatedSprite,
					int pInitialLoopCount) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationFrameChanged(AnimatedSprite pAnimatedSprite,
					int pOldFrameIndex, int pNewFrameIndex) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationLoopFinished(AnimatedSprite pAnimatedSprite,
					int pRemainingLoopCount, int pInitialLoopCount) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationFinished(AnimatedSprite pAnimatedSprite) {
				// TODO Auto-generated method stub
				runOnUpdateThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						mScene.getChildByIndex(LAYER_BACKGROUND).detachChild(
								boom);
						System.gc();
					}

				});
			}

		});
		this.mScene.getChildByIndex(LAYER_BACKGROUND).attachChild(boom);
	}

	private ArrayList<Position> generateCoinPos() {
		ArrayList<Position> positions = new ArrayList<Position>();
		for (int i = 0; i < COIN_NUMBER; i++) {
			int xx = (int) Math.round(Math.random() * CAMERA_WIDTH);
			int yy = (int) Math.round(Math.random() * (CAMERA_HEIGHT - 100));
			Position position = new Position(xx, yy);
			positions.add(position);
		}
		return positions;
	}

	private void onGameStart() {
		this.mScore = 0;
		this.EndingTimer = 60f;
		this.mScoreText.setText("Score: 0");
		this.mTimerText.setText("Time: 0s");
		this.mScene.registerUpdateHandler(collideHandler);
		// 创建金币
		ArrayList<Position> coinsPositions = generateCoinPos();
		coins = new ArrayList<AnimatedSprite>();
		for (Position p : coinsPositions) {
			AnimatedSprite coin = new AnimatedSprite(p.getX(), p.getY(),
					this.mCoinTextureRegion,
					this.getVertexBufferObjectManager());
			coin.animate(100);
			coin.setBlendFunction(GLES20.GL_SRC_ALPHA,
					GLES20.GL_ONE_MINUS_SRC_ALPHA);
			coin.registerUpdateHandler(new GainCoinHandler(coin));
			this.mScene.getChildByIndex(LAYER_SPRITE).attachChild(coin);
			coins.add(coin);
		}
		this.createModifier();

		this.mGameRunning = true;
		if (this.soundEnabled && !this.mBackgroundMusic.isPlaying()) {
			this.mBackgroundMusic.resume();
		}
		this.addBall();
	}

	private void onGameOver() {
		this.ballBody.setLinearVelocity(0, 0);
		this.mGameRunning = false;
		this.mScene.unregisterUpdateHandler(collideHandler);
		if (this.soundEnabled && this.mBackgroundMusic.isPlaying()) {
			this.mBackgroundMusic.pause();
			this.gameOverSound.play();
		}

		if (ball != null) {
			removeBall(true);
		}

		if (this.mScore > this.bestScore) {
			Constants.getInstance(this).setHighScore(mScore);
			this.bestScore = this.mScore;
		}
		showOptions();
	}

	private void showOptions() {
		float width = 320;
		final float height = 200;
		final float y = CAMERA_HEIGHT * 3 / 10;
		final float x = (CAMERA_WIDTH - width) / 2;
		final Rectangle rect = new Rectangle(x, y, width, height,
				this.getVertexBufferObjectManager());
		rect.setColor(0.5f, 0.5f, 0.5f);
		rect.setAlpha(0.8f);

		// 显示本局分数以及最高分的文本
		String content = "Score: " + mScore + "\nBest: " + this.bestScore;
		final Text result = new Text(30, 0, this.mFont, content,
				content.length(), this.getVertexBufferObjectManager());
		result.setHorizontalAlign(HorizontalAlign.CENTER);
		result.setBlendFunction(GLES20.GL_SRC_ALPHA,
				GLES20.GL_ONE_MINUS_SRC_ALPHA);

		// 选项按钮
		float secondRow = 100f;
		final Text retry = new Text(50f, secondRow, this.mFont, "RETRY", 5,
				this.getVertexBufferObjectManager());
		final ButtonSprite retryButton = new ButtonSprite(
				50f + retry.getWidth() + 5f, secondRow + 10f,
				this.mReplayTextureRegion, this.mReplayPressedTextureRegion,
				this.mReplayPressedTextureRegion,
				this.getVertexBufferObjectManager());
		final IEntityModifierListener listener = new IEntityModifierListener() {

			@Override
			public void onModifierStarted(IModifier<IEntity> pModifier,
					IEntity pItem) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onModifierFinished(IModifier<IEntity> pModifier,
					IEntity pItem) {
				// TODO Auto-generated method stub
				runOnUpdateThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						rect.detachChild(result);
						rect.detachChild(retry);
						rect.detachChild(retryButton);
						for(AnimatedSprite coin:coins){
							mScene.getChildByIndex(LAYER_SPRITE).detachChild(coin);
						}
						coins = null;
						mScene.unregisterTouchArea(retryButton);
						mScene.getChildByIndex(LAYER_SCORE).detachChild(rect);
						System.gc();
						onGameStart();
					}
				});

			}

		};
		retryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(ButtonSprite pButtonSprite,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				// TODO Auto-generated method stub
				rect.registerEntityModifier(new MoveModifier(3f, x, x, y,
						-height, listener, EaseBounceIn.getInstance()));
			}

		});
		result.registerEntityModifier(new FadeInModifier(3f));
		retry.registerEntityModifier(new FadeInModifier(3f));
		retryButton.registerEntityModifier(new FadeInModifier(3f));
		rect.registerEntityModifier(new AlphaModifier(3f, 0f, 0.8f));
		rect.attachChild(result);
		rect.attachChild(retry);
		rect.attachChild(retryButton);
		mScene.registerTouchArea(retryButton);
		mScene.getChildByIndex(LAYER_SCORE).attachChild(rect);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public class GainCoinHandler implements IUpdateHandler {
		AnimatedSprite mCoin;

		public GainCoinHandler(AnimatedSprite coin) {
			mCoin = coin;
		}

		@Override
		public void onUpdate(float pSecondsElapsed) {
			if (mCoin.collidesWith(ball)) {
				if (soundEnabled) {
					mCoinSound.play();
				}
				mCoin.registerEntityModifier(entityModifier);// 这个变换几乎是没效果的
				mScene.unregisterTouchArea(mCoin);
				// mScene.getChildByIndex(LAYER_SPRITE).detachChild(coin);
				runOnUpdateThread(new Runnable() {
					@Override
					public void run() {
						mCoin.detachSelf();
					}
				});
				Text mText = new Text(mCoin.getX(), mCoin.getY() - 10, mFont,
						"+5", new TextOptions(HorizontalAlign.CENTER),
						getVertexBufferObjectManager());
				mScene.getChildByIndex(LAYER_SCORE).attachChild(mText);
				LoopEntityModifier textModifier = new LoopEntityModifier(
				// EntityModifier的监听，通知LoopEntityModifier的开始和结束
						null, 1,
						// 循环的监听，通知每次循环的开始和结束
						new ILoopEntityModifierListener() {
							@Override
							public void onLoopStarted(
									final LoopModifier<IEntity> pLoopModifier,
									final int pLoop, final int pLoopCount) {
								PhysicsBall.this.runOnUiThread(new Runnable() {
									@Override
									public void run() {

									}
								});
							}

							@Override
							public void onLoopFinished(
									final LoopModifier<IEntity> pLoopModifier,
									final int pLoop, final int pLoopCount) {
								PhysicsBall.this.runOnUiThread(new Runnable() {
									@Override
									public void run() {

									}
								});
							}
						},
						// 循环Modifier中组合的Modifier，先按顺序执行
						new SequenceEntityModifier(
								new ParallelEntityModifier(new AlphaModifier(5,
										1, 0), new ScaleModifier(5, 1, 0.5f)),
								new DelayModifier(2)
						));
				mText.registerEntityModifier(textModifier);
				mScore += 5;
				mScoreText.setText("Score: " + mScore);
				System.gc();

			}
		}

		@Override
		public void reset() {
			// TODO Auto-generated method stub

		}
	}

}
