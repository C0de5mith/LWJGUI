package lwjgui;

import static org.lwjgl.system.MemoryStack.stackMallocFloat;
import static org.lwjgl.system.MemoryStack.stackPop;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import lwjgui.Color;
import lwjgui.geometry.Insets;
import lwjgui.geometry.Pos;
import lwjgui.gl.GenericShader;
import lwjgui.gl.Renderer;
import lwjgui.scene.Context;
import lwjgui.scene.Window;
import lwjgui.scene.control.CheckBox;
import lwjgui.scene.control.Label;
import lwjgui.scene.layout.BorderPane;
import lwjgui.scene.layout.VBox;

public class OpenGLExample extends LWJGUIApplication {
	public static final int WIDTH   = 320;
	public static final int HEIGHT  = 240;
	
	private static CheckBox spinBox;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(String[] args, Window window) {
		// Create a simple pane
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(24));
		root.setBackground(null); // See through, so we don't block the opengl drawn underneath

		// Set the pane as the scenes root
		window.getScene().setRoot(root);

		// Put labels in pane
		{
			VBox vbox = new VBox();
			vbox.setAlignment(Pos.CENTER);
			vbox.setBackground(Color.BLUE.alpha(0.2f));
			root.setCenter(vbox);

			Label label1 = new Label("Hello World!");
			label1.setTextFill(Color.AQUA);
			vbox.getChildren().add(label1);
			
			Label label2 = new Label("OpenGL drawn straight to window.");
			label2.setTextFill(Color.BLACK);
			vbox.getChildren().add(label2);
			
			Label label3 = new Label("LWJGUI ontop!");
			label3.setTextFill(Color.CORAL);
			vbox.getChildren().add(label3);
		}
		
		// Add a checkbox
		spinBox = new CheckBox("Spin");
		root.setBottom(spinBox);
		
		// Render OpenGL Scene
		window.setRenderingCallback(new RenderingCallbackTest());
	}

	@Override
	public void run() {
		//
	}

	@Override
	public String getProgramName() {
		return "OpenGL Example";
	}

	@Override
	public int getDefaultWindowWidth() {
		return WIDTH;
	}

	@Override
	public int getDefaultWindowHeight() {
		return HEIGHT;
	}
	
	private static class RenderingCallbackTest implements Renderer {
		private GenericShader shader;
		private int vao;
		private int vbo;
		private float rot;

		public RenderingCallbackTest() {
			// Test shader
			shader = new GenericShader(); // Will load a testing vert/frag quad shader
			
			// Setup geometry
			int vertSize = 3; // vec3 in shader
			int texSize = 2; // vec2 in shader
			int colorSize = 4; // vec4 in shader
			int size = vertSize + texSize + colorSize; // Stride length
			int verts = 3; // Number of vertices
			int bytes = Float.BYTES; // Bytes per element (float)
			
			stackPush();
			{
				// Initial vertex data
				FloatBuffer buffer = stackMallocFloat(verts * size);
				buffer.put(-0.5f).put(+0.5f).put(0.0f);		// Vert 1 position
				buffer.put(new float[] {0.0f, 0.0f});		// Vert 1 texture
				buffer.put(new float[] {1.0f,0.0f,0.0f,1.0f}); // Vert 1 color
				
				buffer.put(+0.5f).put(+0.5f).put(0.0f);		// Vert 2 position
				buffer.put(new float[] {0.0f, 0.0f});		// Vert 2 texture
				buffer.put(new float[] {0.0f,1.0f,0.0f,1.0f}); // Vert 2 color
				
				buffer.put(+0.0f).put(-0.5f).put(0.0f);		// Vert 3 position
				buffer.put(new float[] {0.0f, 0.0f});		// Vert 3 texture
				buffer.put(new float[] {0.0f,0.0f,1.0f,1.0f}); // Vert 3 color
				buffer.flip();

				// Generate buffers
				vbo = glGenBuffers();
				vao = glGenVertexArrays();

				// Upload Vertex Buffer
				glBindBuffer(GL_ARRAY_BUFFER, vbo);
				glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

				// Set attributes (automatically stored to currently bound VAO)
				glBindVertexArray(vao);
				glEnableVertexAttribArray(0); // layout 0 shader
				glEnableVertexAttribArray(1); // layout 1 shader
				glEnableVertexAttribArray(2); // layout 2 shader
				int vertOffset = 0;
				glVertexAttribPointer( 0, vertSize,  GL_FLOAT, false, size*bytes, vertOffset );
				int texOffset = vertSize*bytes;
				glVertexAttribPointer( 1, texSize,   GL_FLOAT, false, size*bytes, texOffset );
				int colorOffset = texOffset + texSize*bytes;
				glVertexAttribPointer( 2, colorSize, GL_FLOAT, false, size*bytes, colorOffset );

				// Unbind
				glBindBuffer(GL_ARRAY_BUFFER, 0);
				glBindVertexArray(0);
			}
			stackPop();
		}

		@Override
		public void render(Context context) {
			if ( spinBox.isChecked() ) {
				rot += 1.0e-3f;
			}
			
			// Bind shader for drawing
			shader.bind();
			shader.projectOrtho( -0.6f, -0.6f, 1.2f, 1.2f );
			shader.setWorldMatrix(new Matrix4f().rotateY(rot));

			// Disable culling (just in case)
			GL11.glDisable(GL11.GL_CULL_FACE);
			
			// Render geometry
			glBindVertexArray(vao);
			glDrawArrays(GL_TRIANGLES, 0, 3);
		}
	}
}