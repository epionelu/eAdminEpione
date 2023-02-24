package lu.esante.agence.epione.structure;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractMapper<A, B> {
    public abstract A entityToBusiness(B b);

    public abstract B businessToEntity(A a);

    public List<A> entityToBusiness(List<B> value) {
        return value.stream()
                .map(this::entityToBusiness)
                .collect(Collectors.toList());
    }

    public List<B> businessToEntity(List<A> value) {
        return value.stream()
                .map(this::businessToEntity)
                .collect(Collectors.toList());
    }

    public Optional<A> entityToBusiness(Optional<B> value) {
        if (value.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(entityToBusiness(value.get()));
    }

    public Optional<B> businessToEntity(Optional<A> value) {
        if (value.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(businessToEntity(value.get()));
    }

}
