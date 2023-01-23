# automatically generated by the FlatBuffers compiler, do not modify

# namespace: schema

import flatbuffers
from flatbuffers.compat import import_numpy
np = import_numpy()

# If events are not otherwise delimited, this wrapper structure
# allows a game to be stored in a single buffer.
# The first event will be a GameHeader; the last event will be a GameFooter.
# matchHeaders[0] is the index of the 0th match header in the event stream,
# corresponding to matchFooters[0]. These indices allow quick traversal of
# the file.
class GameWrapper(object):
    __slots__ = ['_tab']

    @classmethod
    def GetRootAs(cls, buf, offset=0):
        n = flatbuffers.encode.Get(flatbuffers.packer.uoffset, buf, offset)
        x = GameWrapper()
        x.Init(buf, n + offset)
        return x

    @classmethod
    def GetRootAsGameWrapper(cls, buf, offset=0):
        """This method is deprecated. Please switch to GetRootAs."""
        return cls.GetRootAs(buf, offset)
    # GameWrapper
    def Init(self, buf, pos):
        self._tab = flatbuffers.table.Table(buf, pos)

    # The series of events comprising the game.
    # GameWrapper
    def Events(self, j):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(4))
        if o != 0:
            x = self._tab.Vector(o)
            x += flatbuffers.number_types.UOffsetTFlags.py_type(j) * 4
            x = self._tab.Indirect(x)
            from battlecode.schema.EventWrapper import EventWrapper
            obj = EventWrapper()
            obj.Init(self._tab.Bytes, x)
            return obj
        return None

    # GameWrapper
    def EventsLength(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(4))
        if o != 0:
            return self._tab.VectorLen(o)
        return 0

    # GameWrapper
    def EventsIsNone(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(4))
        return o == 0

    # The indices of the headers of the matches, in order.
    # GameWrapper
    def MatchHeaders(self, j):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(6))
        if o != 0:
            a = self._tab.Vector(o)
            return self._tab.Get(flatbuffers.number_types.Int32Flags, a + flatbuffers.number_types.UOffsetTFlags.py_type(j * 4))
        return 0

    # GameWrapper
    def MatchHeadersAsNumpy(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(6))
        if o != 0:
            return self._tab.GetVectorAsNumpy(flatbuffers.number_types.Int32Flags, o)
        return 0

    # GameWrapper
    def MatchHeadersLength(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(6))
        if o != 0:
            return self._tab.VectorLen(o)
        return 0

    # GameWrapper
    def MatchHeadersIsNone(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(6))
        return o == 0

    # The indices of the footers of the matches, in order.
    # GameWrapper
    def MatchFooters(self, j):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(8))
        if o != 0:
            a = self._tab.Vector(o)
            return self._tab.Get(flatbuffers.number_types.Int32Flags, a + flatbuffers.number_types.UOffsetTFlags.py_type(j * 4))
        return 0

    # GameWrapper
    def MatchFootersAsNumpy(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(8))
        if o != 0:
            return self._tab.GetVectorAsNumpy(flatbuffers.number_types.Int32Flags, o)
        return 0

    # GameWrapper
    def MatchFootersLength(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(8))
        if o != 0:
            return self._tab.VectorLen(o)
        return 0

    # GameWrapper
    def MatchFootersIsNone(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(8))
        return o == 0

def GameWrapperStart(builder): builder.StartObject(3)
def Start(builder):
    return GameWrapperStart(builder)
def GameWrapperAddEvents(builder, events): builder.PrependUOffsetTRelativeSlot(0, flatbuffers.number_types.UOffsetTFlags.py_type(events), 0)
def AddEvents(builder, events):
    return GameWrapperAddEvents(builder, events)
def GameWrapperStartEventsVector(builder, numElems): return builder.StartVector(4, numElems, 4)
def StartEventsVector(builder, numElems):
    return GameWrapperStartEventsVector(builder, numElems)
def GameWrapperAddMatchHeaders(builder, matchHeaders): builder.PrependUOffsetTRelativeSlot(1, flatbuffers.number_types.UOffsetTFlags.py_type(matchHeaders), 0)
def AddMatchHeaders(builder, matchHeaders):
    return GameWrapperAddMatchHeaders(builder, matchHeaders)
def GameWrapperStartMatchHeadersVector(builder, numElems): return builder.StartVector(4, numElems, 4)
def StartMatchHeadersVector(builder, numElems):
    return GameWrapperStartMatchHeadersVector(builder, numElems)
def GameWrapperAddMatchFooters(builder, matchFooters): builder.PrependUOffsetTRelativeSlot(2, flatbuffers.number_types.UOffsetTFlags.py_type(matchFooters), 0)
def AddMatchFooters(builder, matchFooters):
    return GameWrapperAddMatchFooters(builder, matchFooters)
def GameWrapperStartMatchFootersVector(builder, numElems): return builder.StartVector(4, numElems, 4)
def StartMatchFootersVector(builder, numElems):
    return GameWrapperStartMatchFootersVector(builder, numElems)
def GameWrapperEnd(builder): return builder.EndObject()
def End(builder):
    return GameWrapperEnd(builder)