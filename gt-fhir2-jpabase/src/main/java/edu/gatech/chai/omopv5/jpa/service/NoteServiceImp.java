package edu.gatech.chai.omopv5.jpa.service;

import org.springframework.stereotype.Service;

import edu.gatech.chai.omopv5.jpa.dao.NoteDao;
import edu.gatech.chai.omopv5.jpa.entity.Note;

@Service
public class NoteServiceImp extends BaseEntityServiceImp<Note, NoteDao> implements NoteService {

	public NoteServiceImp() {
		super(Note.class);
	}

}
