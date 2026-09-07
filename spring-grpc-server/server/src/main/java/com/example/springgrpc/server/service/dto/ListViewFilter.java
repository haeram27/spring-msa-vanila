package com.example.springgrpc.server.service.dto;

import com.example.springgrpc.server.repository.enums.Category;
import com.example.springgrpc.server.repository.enums.Frequency;
import com.example.springgrpc.server.repository.enums.Status;

import java.util.List;
import java.util.Set;

/**
 * proto의 {@code FilterNode} oneof에 대응하는 도메인 필터 트리.
 * <p>
 * sealed로 선언해 구현이 이 파일 안에 닫혀 있으므로, 이를 소비하는 쪽은 switch가
 * 모든 분기를 덮었는지 컴파일러에게 검사받을 수 있다. proto에 필터 종류가 추가되면
 * 여기에 레코드를 더하는 순간 매칭 코드가 컴파일 에러로 드러난다.
 * <p>
 * 판정 로직은 담지 않는다. 순수 데이터로 두고 {@code ListViewSampleService}가 해석한다.
 */
public sealed interface ListViewFilter {

    /** 하위 필터를 모두 만족해야 한다. targets가 비면 아무것도 거르지 않는다. */
    record And(List<ListViewFilter> targets) implements ListViewFilter {
        public And {
            targets = List.copyOf(targets);
        }
    }

    /** 이름에 대한 부분 일치(대소문자 무시). */
    record SearchString(String value) implements ListViewFilter {
    }

    record StatusIn(Set<Status> values) implements ListViewFilter {
        public StatusIn {
            values = Set.copyOf(values);
        }
    }

    record CategoryIn(Set<Category> values) implements ListViewFilter {
        public CategoryIn {
            values = Set.copyOf(values);
        }
    }

    record FrequencyIn(Set<Frequency> values) implements ListViewFilter {
        public FrequencyIn {
            values = Set.copyOf(values);
        }
    }
}
