package parse.dont.validate;

import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.collection.List;
import io.vavr.test.Arbitrary;
import io.vavr.test.Gen;
import io.vavr.test.Property;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static io.vavr.API.println;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static parse.dont.validate.NIRGenerator.validNIR;

class NIRMutatedProperties {
    private static final Random random = new Random();

    private record Mutator(String name, Function1<NIR, Gen<String>> func) {
        public String mutate(NIR nir) {
            return func.apply(nir).apply(random);
        }
    }

    private static Mutator sexMutator = new Mutator("Sex mutator", nir ->
            Gen.choose(3, 9).map(invalidSex -> invalidSex + nir.toString().substring(1))
    );

    private static Mutator yearMutator = new Mutator("Year mutator", nir ->
            Gen.frequency(
                    Tuple.of(7, Gen.choose(100, 999)),
                    Tuple.of(3, Gen.choose(1, 9))
            ).map(invalidYear -> concat(
                            nir.toString().charAt(0),
                            invalidYear.toString(),
                            nir.toString().substring(3)
                    )
            )
    );

    private static String concat(Object... elements) {
        return List.of(elements).mkString();
    }

    private static Mutator truncateMutator = new Mutator("Truncate mutator", nir ->
            Gen.choose(1, 13).map(size ->
                    size == 1 ? "" : nir.toString().substring(0, size - 1)
            )
    );

    private static Arbitrary<Mutator> mutators = Gen.choose(
            sexMutator,
            yearMutator,
            truncateMutator
    ).arbitrary();

    @Test
    void invalidNIRCanNeverBeParsed() {
        Property.def("parseNIR(nir.ToString()) == nir")
                .forAll(validNIR, mutators)
                .suchThat(NIRMutatedProperties::canNotParseMutatedNIR)
                .check()
                .assertIsSatisfied();
    }

    @Test
    void test() {
        assertThat(NIR.parseNIR("191100262355124"))
                .isLeft();
    }

    private static boolean canNotParseMutatedNIR(NIR nir, Mutator mutator) {
        var mutatedNIR = mutator.mutate(nir);
        println("NIR: " + nir + " Mutator: " + mutator.name + " / " + mutatedNIR);
        return NIR.parseNIR(mutatedNIR).isLeft();
    }
}