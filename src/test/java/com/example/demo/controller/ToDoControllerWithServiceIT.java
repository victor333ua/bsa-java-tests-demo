package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.Arrays;
import java.util.Optional;

import com.example.demo.model.ToDoEntity;
import com.example.demo.repository.ToDoRepository;
import com.example.demo.service.ToDoService;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ToDoController.class)
@ActiveProfiles(profiles = "test")
@Import(ToDoService.class)
class ToDoControllerWithServiceIT {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ToDoRepository toDoRepository;

	@Test
	void whenGetAll_thenReturnValidResponse() throws Exception {
		String testText = "My to do text";
		when(toDoRepository.findAll()).thenReturn(
			Arrays.asList(
				new ToDoEntity(1l, testText)
			)
		);
		
		this.mockMvc
			.perform(get("/todos"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].text").value(testText))
			.andExpect(jsonPath("$[0].id").isNumber())
			.andExpect(jsonPath("$[0].completedAt").doesNotExist());
	}

	@Test
	void saveNew() throws Exception {
		Long id = 1234L;
		when(toDoRepository.save(ArgumentMatchers.any(ToDoEntity.class)))
				.thenAnswer(i -> {
					ToDoEntity arg = i.getArgument(0, ToDoEntity.class);
					return new ToDoEntity(id, arg.getText());
				});

		String text = "test mvc";
		this.mockMvc
				.perform(post("/todos")
						.contentType(MediaType.APPLICATION_JSON)
						.characterEncoding("UTF8")
						.content("{\"text\": \"" + text + "\"}")
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.text").value(text))
				.andDo(print());
	}

	@Test
	void saveExisting() throws Exception {

		when(toDoRepository.findById(anyLong())).thenAnswer(i -> {
			Long id = i.getArgument(0, Long.class);
			if( id != 1L && id != 2L) return Optional.empty();
			return Optional.of(new ToDoEntity(id, null));
		});

		when(toDoRepository.save(ArgumentMatchers.any(ToDoEntity.class)))
				.thenAnswer(i -> {
					ToDoEntity arg = i.getArgument(0, ToDoEntity.class);
					return arg;
				});

		String text = "test mvc";
		this.mockMvc
				.perform(post("/todos")
						.contentType(MediaType.APPLICATION_JSON)
						.characterEncoding("UTF8")
						.content("{\"id\": 1, \"text\": \"" + text + "\"}")
				)
				.andExpect(status().isOk())
				.andDo(print())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.text").value(text));

		this.mockMvc
				.perform(post("/todos")
						.contentType(MediaType.APPLICATION_JSON)
						.characterEncoding("UTF8")
						.content("{\"id\": 3, \"text\": \"" + text + "\"}")
				)
				.andDo(print());
	}

	@Test
	void testUpsert() throws Exception {
		when(toDoRepository.findById(anyLong())).thenAnswer(i -> {
			Long id = i.getArgument(0, Long.class);
			if( id != 1L && id != 2L) return Optional.empty();
			return Optional.of(new ToDoEntity(id, null));
		});

		Long idNew = 1234L;
		when(toDoRepository.save(ArgumentMatchers.any(ToDoEntity.class)))
				.thenAnswer(i -> {
					ToDoEntity arg = i.getArgument(0, ToDoEntity.class);
					if (arg.getId() == null)
						return new ToDoEntity(idNew, arg.getText());
					return arg;
				});

		String text = "test mvc";
		this.mockMvc
				.perform(post("/todos")
						.contentType(MediaType.APPLICATION_JSON)
						.characterEncoding("UTF8")
						.content("{\"text\": \"" + text + "\"}")
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(idNew))
				.andExpect(jsonPath("$.text").value(text))
				.andDo(print());

		this.mockMvc
				.perform(post("/todos")
						.contentType(MediaType.APPLICATION_JSON)
						.characterEncoding("UTF8")
						.content("{\"id\": 1, \"text\": \"" + text + "\"}")
				)
				.andExpect(status().isOk())
				.andDo(print())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.text").value(text));

		this.mockMvc
				.perform(post("/todos")
						.contentType(MediaType.APPLICATION_JSON)
						.characterEncoding("UTF8")
						.content("{\"id\": 3, \"text\": \"" + text + "\"}")
				)
				.andDo(print());

	}

}
