package br.com.reinan.dscatalog.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.reinan.dscatalog.dto.CategoryDTO;
import br.com.reinan.dscatalog.entities.Category;
import br.com.reinan.dscatalog.repositories.CategoryRepository;
import br.com.reinan.dscatalog.services.exceptions.ResorceNotFoundException;
import br.com.reinan.dscatalog.tests.Factory;

@ExtendWith(SpringExtension.class)
public class CategoryServiceTests {

    @Mock
    private CategoryRepository repository;

    @InjectMocks
    private CategoryServiceImpl service;

    private Long existsId;
    private Long notExistsId;
    private Category category;
    private CategoryDTO dto;
    private PageImpl<Category> page;

    @BeforeEach
    public void setUp() {

        dto = Factory.createCategoryDto();
        existsId = 1L;
        category = Factory.createCategory();
        notExistsId = 1000L;
        page = new PageImpl<>(List.of(category));
        doNothing().when(repository).deleteById(existsId);
        doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(notExistsId);
        when(repository.findById(existsId)).thenReturn(Optional.of(category));
        when(repository.findById(notExistsId)).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(category);
        when(repository.findAll((Pageable) any())).thenReturn(page);
    }

    @Test
    public void deleteShouldDoNothing() {
        Assertions.assertDoesNotThrow(() -> {
            service.delete(existsId);
        });
        verify(repository).deleteById(existsId);
    }

    @Test
    public void deleteShouldThrowsEmptyResultDataAccessException() {
        Assertions.assertThrows(ResorceNotFoundException.class, () -> {
            service.delete(notExistsId);
        });

        verify(repository).deleteById(notExistsId);
    }

    @Test
    public void findByIdShouldReturnOptionalNotEmptyWhenIdExists() {
        CategoryDTO dto = service.findById(existsId);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals("category", dto.getName());

        verify(repository).findById(existsId);
    }

    @Test
    public void findByIdShouldReturnOptionalEmptyWhenIdExists() {
        Assertions.assertThrows(ResorceNotFoundException.class, () -> {
            service.findById(notExistsId);
        });

        verify(repository).findById(notExistsId);
    }

    @Test
    public void insertShouldPersitObjectInDataBase() {
        Assertions.assertDoesNotThrow(() -> {
            service.insert(dto);
        });

        verify(repository).save(any());
    }

    @Test
    public void updateShouldReturnEntityUpadate() {
        Assertions.assertDoesNotThrow(() -> {
            service.update(existsId, dto);
        });

        verify(repository).findById(existsId);
        verify(repository).save(any());
    }

    @Test
    public void updateShouldThrowsResorceNotFoundExceptionWhenNotExistsId() {
        Assertions.assertThrows(ResorceNotFoundException.class, () -> {
            service.update(notExistsId, dto);
        });

        verify(repository).findById(notExistsId);

    }

    @Test
    public void findAllShouldReturnPage() {
        Page<CategoryDTO> pageImpl = service.findAll(PageRequest.of(0, 10));

        Assertions.assertNotNull(pageImpl);
        Assertions.assertEquals(pageImpl.getNumber(), 0);
        Assertions.assertEquals(pageImpl.getSize(), 1);
        Assertions.assertEquals(pageImpl.getContent().get(0).getName(), "category");
    }

}
