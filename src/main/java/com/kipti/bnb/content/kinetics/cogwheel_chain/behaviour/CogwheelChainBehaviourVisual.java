package com.kipti.bnb.content.kinetics.cogwheel_chain.behaviour;

import com.cake.azimuth.behaviour.extensions.RenderedBehaviourExtension;
import com.cake.struts.content.IAntiClippedShadowLighter;
import com.kipti.bnb.content.kinetics.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.kinetics.cogwheel_chain.render.ChainQuadBuilder;
import com.kipti.bnb.content.kinetics.cogwheel_chain.render.CogwheelChainRenderGeometryBuilder;
import com.kipti.bnb.content.kinetics.cogwheel_chain.render.CogwheelChainRenderGeometryBuilder.ChainSegment;
import com.kipti.bnb.content.kinetics.cogwheel_chain.types.CogwheelChainType;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.processing.burner.ScrollTransformedInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.vertex.MutableVertexList;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.material.Materials;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.model.QuadMesh;
import dev.engine_room.flywheel.lib.model.SingleMeshModel;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

//TODO: fix light updates
public class CogwheelChainBehaviourVisual extends RenderedBehaviourExtension.BehaviourVisual {

    private final VisualizationContext context;
    private final KineticBlockEntity kineticBlockEntity;
    private final CogwheelChainBehaviour cogwheelChainBehaviour;

    @Nullable
    private ScrollTransformedInstance chainInstance;

    private int chainSignature = Integer.MIN_VALUE;
    private float textureSquish = 1;
    private float lastSpeedV = Float.NaN;
    private int lastPackedLight = Integer.MIN_VALUE;

    public CogwheelChainBehaviourVisual(final VisualizationContext context, final KineticBlockEntity kineticBlockEntity, final CogwheelChainBehaviour cogwheelChainBehaviour, final AbstractBlockEntityVisual<?> parentVisual) {
        super(parentVisual);
        this.context = context;
        this.kineticBlockEntity = kineticBlockEntity;
        this.cogwheelChainBehaviour = cogwheelChainBehaviour;

        this.rebuildMeshIfNeeded(true);
        this.updateLight(0);
        this.update(0);
    }

    @Override
    public void update(final float partialTick) {
        this.rebuildMeshIfNeeded(false);

        if (this.chainInstance == null) {
            return;
        }

        final float rotationsPerTick = this.cogwheelChainBehaviour.getChainRotationFactor() * this.kineticBlockEntity.getSpeed() / (60 * 20);
        final float speedV = (float) (Math.PI * 2 * rotationsPerTick * this.textureSquish);

        if (!Mth.equal(this.lastSpeedV, speedV)) {
            this.chainInstance.speed(0, speedV);
            this.chainInstance.scaleU = 0;
            this.chainInstance.scaleV = 1;
            this.chainInstance.offsetU = 0;
            this.chainInstance.offsetV = 0;
            this.chainInstance.diffU = 0;
            this.chainInstance.diffV = 0;
            this.chainInstance.setChanged();
            this.lastSpeedV = speedV;
        }
    }

    @Override
    public void updateLight(final float partialTick) {
        if (this.chainInstance == null || this.kineticBlockEntity.getLevel() == null) {
            return;
        }

        final int packedLight = LevelRenderer.getLightColor(this.kineticBlockEntity.getLevel(), this.kineticBlockEntity.getBlockPos());
        if (packedLight != this.lastPackedLight) {
            this.chainInstance.light(packedLight);
            this.chainInstance.setChanged();
            this.lastPackedLight = packedLight;
        }
    }

    @Override
    public void collectCrumblingInstances(final Consumer<Instance> consumer) {
        if (this.chainInstance != null) {
            consumer.accept(this.chainInstance);
        }
    }

    @Override
    public void delete() {
        this.deleteInstance();
    }

    private void rebuildMeshIfNeeded(final boolean force) {
        final CogwheelChain chain = this.cogwheelChainBehaviour.getControlledChain();
        if (chain == null) {
            this.deleteInstance();
            this.chainSignature = Integer.MIN_VALUE;
            return;
        }

        final CogwheelChainType chainType = chain.getChainType();
        final boolean flipInsideOutside = chainType.getRenderType().usesConsistentInsideOutside() && chain.shouldFlipInsideOutside();
        final int newSignature = Objects.hash(chain.hashCode(), chainType.getKey(), flipInsideOutside);
        if (!force && newSignature == this.chainSignature && this.chainInstance != null) {
            return;
        }

        final List<ChainSegment> segments = CogwheelChainRenderGeometryBuilder.buildSegments(chain, Vec3.ZERO);
        double totalChainDistance = 0;
        for (final ChainSegment segment : segments) {
            totalChainDistance += segment.distance();
        }

        if (totalChainDistance <= 1e-4) {
            this.deleteInstance();
            this.chainSignature = newSignature;
            return;
        }

        this.textureSquish = (float) (Math.ceil(totalChainDistance) / totalChainDistance);
        final Function<Vector3f, Integer> lighter = IAntiClippedShadowLighter.createGlobalLighter(this.kineticBlockEntity);

        final CogwheelChainMesh mesh = new CogwheelChainMesh(
                segments,
                chainType,
                flipInsideOutside,
                this.textureSquish,
                this.kineticBlockEntity.getBlockPos().getX(),
                this.kineticBlockEntity.getBlockPos().getY(),
                this.kineticBlockEntity.getBlockPos().getZ(),
                lighter
        );
        // SQUARE shapes form a closed tube — enable backface culling to avoid rendering
        // the interior faces. CROSS shapes need both sides visible.
        final boolean isCross = chainType.getRenderType().getVertexShape() == CogwheelChainType.VertexShape.CROSS;
        final SimpleMaterial material = SimpleMaterial.builderOf(Materials.CUTOUT_MIPPED_BLOCK)
                .texture(chainType.getRenderTexture())
                .backfaceCulling(!isCross)
                .build();

        this.deleteInstance();

        this.chainInstance = this.context.instancerProvider()
                .instancer(AllInstanceTypes.SCROLLING_TRANSFORMED, new SingleMeshModel(mesh, material))
                .createInstance();
        this.chainInstance
                .setIdentityTransform()
                .translate(this.getVisualPosition().getX(), this.getVisualPosition().getY(), this.getVisualPosition().getZ());

        this.chainInstance.overlay(OverlayTexture.NO_OVERLAY);
        this.chainInstance.setChanged();
        this.chainSignature = newSignature;
        this.lastSpeedV = Float.NaN;
        this.lastPackedLight = Integer.MIN_VALUE;
    }

    private void deleteInstance() {
        if (this.chainInstance != null) {
            this.chainInstance.delete();
            this.chainInstance = null;
        }
    }

    private static class CogwheelChainMesh implements QuadMesh {

        private final List<Vertex> vertices;
        private final Vector4fc boundingSphere;

        private CogwheelChainMesh(final List<ChainSegment> segments,
                                  final CogwheelChainType type,
                                  final boolean flipInsideOutside,
                                  final float textureSquish,
                                  final int baseX,
                                  final int baseY,
                                  final int baseZ,
                                  final Function<Vector3f, Integer> lighter) {
            final ArrayList<Vertex> builtVertices = new ArrayList<>();
            final Bounds bounds = new Bounds();
            final CogwheelChainType.ChainRenderInfo chainRenderInfo = type.getRenderType();

            final Matrix3f accumulatedOrientation = new Matrix3f();

            for (final ChainSegment segment : segments) {
                List<Vec3> destinationPoints = CogwheelChainRenderGeometryBuilder.getEndPointsForChainJoint(
                        segment.from(),
                        segment.to(),
                        segment.postTo(),
                        chainRenderInfo,
                        segment.toCogwheelAxis(),
                        accumulatedOrientation
                );
                if (segment.fromCogwheelAxis().dot(segment.toCogwheelAxis()) < 0.99) {
                    //Let the axes in accumulatedOrientation be relative
                    // Z = forwards / averagedir
                    // Y = perpendicular to forwards and cogwheel axis, this is the radius axis
                    // X = cogwheel axis

                    //We need to rotate current X and Y around the Z axis to ensure that when we 'roll' (i.e. cogwheel axis goes from world x to world y) the generated geometry is consistent
                    final int rotationSign = segment.fromCogwheelAxis().cross(segment.toCogwheelAxis()).dot(segment.to().subtract(segment.from())) > 0 ? 1 : -1;
                    accumulatedOrientation.mul(new Matrix3f(
                            0, rotationSign, 0,
                            -rotationSign, 0, 0,
                            0, 0, 1
                    ));
                }
                final List<Vec3> sourcePoints = CogwheelChainRenderGeometryBuilder.getEndPointsForChainJoint(
                        segment.preFrom(),
                        segment.from(),
                        segment.to(),
                        chainRenderInfo,
                        segment.fromCogwheelAxis(),
                        accumulatedOrientation
                );

                destinationPoints = CogwheelChainRenderGeometryBuilder.getPointsInClosestOrder(destinationPoints, sourcePoints);

                final float minV = (float) (segment.uvStart() * textureSquish);
                final float maxV = (float) ((segment.uvStart() + segment.distance()) * textureSquish);

                ChainQuadBuilder.buildSegmentFaces(destinationPoints, sourcePoints, chainRenderInfo, minV, maxV, flipInsideOutside,
                        (x, y, z, u, v, nx, ny, nz) -> {
                            final int light = lighter.apply(new Vector3f(x + baseX, y + baseY, z + baseZ));
                            builtVertices.add(new Vertex(x, y, z, u, v, nx, ny, nz, light));
                            bounds.include(x, y, z);
                        }, false);
            }

            this.vertices = builtVertices;
            this.boundingSphere = bounds.toBoundingSphere(builtVertices);
        }

        @Override
        public int vertexCount() {
            return this.vertices.size();
        }

        @Override
        public void write(final MutableVertexList vertexList) {
            for (int i = 0; i < this.vertices.size(); i++) {
                final Vertex vertex = this.vertices.get(i);
                vertexList.x(i, vertex.x);
                vertexList.y(i, vertex.y);
                vertexList.z(i, vertex.z);
                vertexList.u(i, vertex.u);
                vertexList.v(i, vertex.v);
                vertexList.normalX(i, 0);
                vertexList.normalY(i, 1);
                vertexList.normalZ(i, 0);
                vertexList.r(i, 1);
                vertexList.g(i, 1);
                vertexList.b(i, 1);
                vertexList.a(i, 1);
                vertexList.light(i, vertex.light);
                vertexList.overlay(i, OverlayTexture.NO_OVERLAY);
            }
        }

        @Override
        public Vector4fc boundingSphere() {
            return this.boundingSphere;
        }

        private record Vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz, int light) {
        }

        private static class Bounds {
            private float minX = Float.POSITIVE_INFINITY;
            private float minY = Float.POSITIVE_INFINITY;
            private float minZ = Float.POSITIVE_INFINITY;
            private float maxX = Float.NEGATIVE_INFINITY;
            private float maxY = Float.NEGATIVE_INFINITY;
            private float maxZ = Float.NEGATIVE_INFINITY;

            void include(final float x, final float y, final float z) {
                if (x < this.minX) this.minX = x;
                if (y < this.minY) this.minY = y;
                if (z < this.minZ) this.minZ = z;
                if (x > this.maxX) this.maxX = x;
                if (y > this.maxY) this.maxY = y;
                if (z > this.maxZ) this.maxZ = z;
            }

            Vector4fc toBoundingSphere(final List<Vertex> vertices) {
                if (vertices.isEmpty()) {
                    return new Vector4f(0, 0, 0, 0.01f);
                }

                final float cx = (this.minX + this.maxX) * 0.5f;
                final float cy = (this.minY + this.maxY) * 0.5f;
                final float cz = (this.minZ + this.maxZ) * 0.5f;
                float r2 = 0;
                for (final Vertex vertex : vertices) {
                    final float dx = vertex.x - cx;
                    final float dy = vertex.y - cy;
                    final float dz = vertex.z - cz;
                    r2 = Math.max(r2, dx * dx + dy * dy + dz * dz);
                }

                return new Vector4f(cx, cy, cz, (float) Math.sqrt(r2));
            }
        }
    }
}
